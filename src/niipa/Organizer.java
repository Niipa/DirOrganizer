package niipa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Organizes a download directory.
 *
 * For personal use.
 */
public class Organizer{

  public static void main(String args[]) throws IOException{

    if(args.length != 1){
      System.err.println("Usage: DirOrganizer [Path]");
      System.exit(1);
    }

    File objF = new File(args[0]);

    try{
      objF.exists();
    }catch(SecurityException ex){
      System.err.println("No read access.");
      System.exit(2);
    }

    File arrFiles[] = objF.listFiles();
    HashMap<String, File> hmDirectories = new HashMap(arrFiles.length);


    Pattern pat1 = Pattern.compile("\\[.*?\\].*?-[_\\s]\\d{1,2}(?:v\\d{1})?[_\\s][\\(\\[].*\\](\\.mkv|\\.mp4)"),
            pat2 = Pattern.compile("\\[.*?\\]_?"),
            pat3 = Pattern.compile("[_\s]-[_\s]\\d{2}");
    Matcher mat;
    String strFName;

    for(int itr = 0; itr < arrFiles.length; ++itr){

      strFName = arrFiles[itr].getName();

      if(arrFiles[itr].isDirectory()){

        if(hmDirectories.containsKey(strFName)){
          moveFiles(hmDirectories.get(strFName), arrFiles[itr]);
        }
        else{
          hmDirectories.put(strFName, arrFiles[itr]);
        }
      }
      else{
        //Get a matcher object for this new filename.
        mat = pat1.matcher(strFName);
        //Is this an mkv or mp4?
        if(mat.matches()){

          //Logic to extract the actual name, may be a good idea to wrap this
          //logic within its own method. Do we need to check that this file is valid
          //before extracting the name or can we extract the name while checking validation?

          //strip the [<GroupName>] block
          mat = pat2.matcher(strFName);
          mat.find();
          strFName = strFName.substring(mat.end());

          //strip everything past the <Name> block
          mat = pat3.matcher(strFName);
          mat.find();
          strFName = strFName.substring(0, mat.start());

          //strip leading and trailing whitespace.
          strFName = strFName.trim();
          //strip periods in the name because directories cannot have periods in the name
          strFName = strFName.replaceAll("\\.", "");

          //If we've seen this directory before, put this file in the directory.
          if(hmDirectories.containsKey(strFName)){
            moveFiles(hmDirectories.get(strFName), arrFiles[itr]);
          }
          //Otherwise create a directory and put this file into that directory.
          else{

            File fMoveHere = new File(args[0] + "/" + strFName);
            fMoveHere.mkdir();

            moveFiles(fMoveHere, arrFiles[itr]);
            hmDirectories.put(strFName, fMoveHere);

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
    if(files != null) {
      for (File file : files) {
        Files.move(file.toPath(), moveTo.toPath());
        System.out.println(strMoveFrom + " moved to " + strMoveTo + ".");
      }
    } else{
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
