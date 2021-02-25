package niipa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Organizes a download directory.
 *
 * For personal use.
 */
public class Organizer{

  public static void main(String args[]) throws IOException{

    if(args.length != 1) {
      System.err.println("Usage: DirOrganizer [Path]");
      System.exit(1);
    }

    File src = new File(args[0]);

    try {
      src.exists();
    } catch(SecurityException ex) {
      System.err.println("No read access.");
      System.exit(2);
    }

    File allFiles[] = Objects.requireNonNull(src.listFiles());
    HashMap<String, File> hmDirectories = new HashMap(allFiles.length);


    Pattern entireName = Pattern.compile("\\[.*?\\].*?-[_\\s]\\d{1,2}(?:v\\d{1})?[_\\s][\\(\\[].*\\](\\.mkv|\\.mp4)"),
            group = Pattern.compile("\\[.*?\\]_?"),
            episode = Pattern.compile("[_\s]-[_\s]\\d{2}");
    Matcher mat;
    String series;

    for (int itr = 0; itr < allFiles.length; ++itr) {
      series = allFiles[itr].getName();
      if (allFiles[itr].isDirectory()) {
        if (hmDirectories.containsKey(series)) {
          moveFiles(hmDirectories.get(series), allFiles[itr]);
        } else {
          hmDirectories.put(series, allFiles[itr]);
        }
      } else {
        //Get a matcher object for this new entireName.
        mat = entireName.matcher(series);
        if(mat.matches()){

          //strip the [<GroupName>] block
          mat = group.matcher(series);
          mat.find();
          series = series.substring(mat.end());

          //strip everything past the <Name> block
          mat = episode.matcher(series);
          mat.find();
          series = series.substring(0, mat.start());

          //strip leading and trailing whitespace.
          series = series.trim();
          //strip periods in the name because directories cannot have periods in the name
          series = series.replaceAll("\\.", "");

          //If we've seen this directory before, put this file in the directory.
          if (hmDirectories.containsKey(series)) {
            moveFiles(hmDirectories.get(series), allFiles[itr]);
          } else {
            File fMoveHere = new File(args[0] + "/" + series);
            fMoveHere.mkdir();

            moveFiles(fMoveHere, allFiles[itr]);
            hmDirectories.put(series, fMoveHere);

          }
        }
      }
    }
  }

  // Take a abstract file name moveFrom and move it to directory moveTo.
  // moveFrom may be a directory or file.
  private static void moveFiles(File moveTo, File moveFrom) throws IOException {
    File files[] = moveFrom.listFiles();
    String strMoveTo = moveTo.getName(), strMoveFrom = moveFrom.getName();

    // If moveFrom is a directory
    if (files != null) {
      for (File file : files) {
        Files.move(file.toPath(), moveTo.toPath());
        System.out.println(strMoveFrom + " moved to " + strMoveTo + ".");
      }
    } else {
      Path moveToPath = moveTo.toPath().resolve(moveFrom.getName());
      if (!moveToPath.toFile().exists()) {
        Files.move(moveFrom.toPath(), moveToPath);
        System.out.println(strMoveFrom + " moved to " + strMoveTo + ".");
      } else {
        Path source = moveFrom.toPath();
        Files.move(source, Paths.get(source.getRoot().toString(), "duplicates", source.getFileName().toString()));
        System.out.printf("Moved %s to duplicate folder%n", strMoveFrom);
      }
    }
  }
}
