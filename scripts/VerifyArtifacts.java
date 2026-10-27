import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Standalone Java 21 verifier; no Minecraft classes are loaded. */
class VerifyArtifacts {
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    static byte[] bytes(ZipFile zip, String name) throws Exception {
        var entry = zip.getEntry(name);
        require(entry != null, "Missing " + name + " in " + zip.getName());
        try (var input = zip.getInputStream(entry)) { return input.readAllBytes(); }
    }
    static int verify(ZipFile zip, String loader, int major) throws Exception {
        String metadata = loader.equals("forge") ? "META-INF/mods.toml" : "META-INF/neoforge.mods.toml";
        String other = loader.equals("forge") ? "META-INF/neoforge.mods.toml" : "META-INF/mods.toml";
        require(!new String(bytes(zip, metadata), java.nio.charset.StandardCharsets.UTF_8).contains("${"), "Unexpanded metadata");
        require(zip.getEntry(other) == null, "Foreign loader metadata in " + zip.getName());
        require(zip.getEntry("com/radaeli/ruilib/" + (loader.equals("forge") ? "neoforge" : "forge") + "/RUiLib.class") == null,
                "Foreign bootstrap packaged");
        String pack = new String(bytes(zip, "pack.mcmeta"), java.nio.charset.StandardCharsets.UTF_8);
        require(pack.contains(loader.equals("forge") ? "15" : "34") && !pack.contains("${"), "Incorrect resource pack format");
        int count = 0;
        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            var entry = entries.nextElement();
            if (!entry.getName().endsWith(".class")) continue;
            byte[] data = bytes(zip, entry.getName());
            int actual = (data[6] & 255) << 8 | data[7] & 255;
            require(actual == major, "Incorrect Java bytecode in " + entry.getName() + ": " + actual);
            count++;
        }
        require(count > 50, "Missing toolkit classes");
        for (String component : List.of("UiScreen", "Select", "Slider", "TextField", "SearchField", "ScrollView", "InventoryGrid", "Modal", "ToastHost")) {
            bytes(zip, "com/radaeli/ruilib/ui/" + component + ".class");
        }
        return count;
    }
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        String version = args[1];
        try (var forge = new ZipFile(root.resolve("forge/build/libs/r-ui-lib-forge-" + version + ".jar").toFile());
             var neo = new ZipFile(root.resolve("neoforge/build/libs/r-ui-lib-neoforge-" + version + ".jar").toFile());
             var forgeSources = new ZipFile(root.resolve("forge/build/libs/r-ui-lib-forge-" + version + "-sources.jar").toFile());
             var neoSources = new ZipFile(root.resolve("neoforge/build/libs/r-ui-lib-neoforge-" + version + "-sources.jar").toFile())) {
            int forgeClasses = verify(forge, "forge", 61);
            int neoClasses = verify(neo, "neoforge", 65);
            require(forgeClasses == neoClasses, "Different packaged class counts");
            int sourceCount = 0;
            try (var paths = Files.walk(root.resolve("src/main/java"))) {
                for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                    String name = root.resolve("src/main/java").relativize(path).toString().replace('\\', '/');
                    require(Arrays.equals(bytes(forgeSources, name), bytes(neoSources, name)), "Different shared source: " + name);
                    sourceCount++;
                }
            }
            var entries = forge.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.getName().startsWith("assets/") && !entry.isDirectory()) {
                    require(Arrays.equals(bytes(forge, entry.getName()), bytes(neo, entry.getName())), "Different asset: " + entry.getName());
                }
            }
            String report = "{\"passed\":true,\"forgeJava\":17,\"neoforgeJava\":21,\"classesPerLoader\":" + forgeClasses
                    + ",\"identicalSharedSources\":" + sourceCount + "}";
            Path output = root.resolve("build/reports/artifacts.json");
            Files.createDirectories(output.getParent());
            Files.writeString(output, report);
            System.out.println(report);
        }
    }
}
