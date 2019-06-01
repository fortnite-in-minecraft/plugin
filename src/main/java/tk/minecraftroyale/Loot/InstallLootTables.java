package tk.minecraftroyale.Loot;

import org.bukkit.World;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InstallLootTables {
    public void installLootTables(World w){
        InputStream is = getClass().getResourceAsStream("minecraftroyale.zip");
        ZipInputStream zis = new ZipInputStream(is);

        try {

            ZipEntry entry;

            /*
             Read each entry from the ZipInputStream until no
             more entry found indicated by a null return value
             of the getNextEntry() method.
            */
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Unzipping: " + entry.getName());

                int size;
                byte[] buffer = new byte[2048];

                try (FileOutputStream fos =
                             new FileOutputStream(entry.getName());
                     BufferedOutputStream bos =
                             new BufferedOutputStream(fos, buffer.length)) {

                    while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, size);
                    }
                    bos.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
