/*********************************************************************************
    The MIT License (MIT)

    Copyright (c) 2015 Menard Z. Soliven

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
**********************************************************************************/

package generics.ren;

import java.io.File;
import java.util.HashMap;
import java.util.regex.*;

/*

  Some notes:
  1. Translator groups usually have their files named as such:
   [<GroupName>] <Name> - <Two digit episode number> [<Quality>]<file extension>. Where
   the brackets are literals. This module works on this assumption and will break if this format
   for shows is not followed.

  2. I must've been really bored to make this.

 */

public class Organizer {

  public static void main(String args[]){

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


    Pattern pat1 = Pattern.compile("\\[.*?\\]_?.* - \\d{2} \\[.*\\](\\.mkv|\\.mp4)"),
            pat2 = Pattern.compile("\\[.*?\\]_?"),
            pat3 = Pattern.compile(" - \\d{2}");
    Matcher mat;
    String strProcessedFileName,
           strFName;

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
  private static void moveFiles(File moveTo, File moveFrom){

    File files[] = moveFrom.listFiles();
    String strMoveTo = moveTo.getName(),
           strMoveFrom = moveFrom.getName();

    // If moveFrom is a directory
    if(files != null) {
      for (File file : files) {
        file.renameTo(new File(moveTo + File.separator + file.getName()));
        System.out.println(strMoveFrom + " moved to " + strMoveTo + ".");
      }
    }
    // else moveFrom is not a directory.
    else{
      moveFrom.renameTo(new File(moveTo + File.separator + moveFrom.getName()));
      System.out.println(strMoveFrom + " moved to " + strMoveTo + ".");
    }
  }
}
