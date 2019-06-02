package tk.minecraftroyale.Loot;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.file.Paths;

public class InstallLootTables {
    public void installLootTables(World w, CommandSender sender) throws IOException{
        System.out.println(w.getWorldFolder());
        Path basePath = Paths.get(w.getWorldFolder().toString(), "datapacks");
        InstallLootTables.deleteFolder(Paths.get(basePath.toString(), "minecraftroyale").toFile());

        InputStream is = InstallLootTables.class.getResourceAsStream("/minecraftroyale.zip");
        if(is == null){
            throw new IOException("InputStream was null!");
        }
        ZipInputStream zis = new ZipInputStream(is);


        ZipEntry entry;

        /*
         Read each entry from the ZipInputStream until no
         more entry found indicated by a null return value
         of the getNextEntry() method.
        */
        while ((entry = zis.getNextEntry()) != null) {
            if(entry.isDirectory()) {
                Paths.get(basePath.toString(), entry.getName()).toFile().mkdirs();
            }else{
                System.out.println("Unzipping: " + entry.getName());

                int size;
                byte[] buffer = new byte[2048];

                try (FileOutputStream fos =
                             new FileOutputStream(Paths.get(basePath.toString(), entry.getName()).toFile());
                     BufferedOutputStream bos =
                             new BufferedOutputStream(fos, buffer.length)) {

                    while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, size);
                    }
                    bos.flush();
                }
            }
        }

        Bukkit.dispatchCommand(sender, "minecraft:reload");
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
