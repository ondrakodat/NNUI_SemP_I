package MazeEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
/*
* This class represents our Maze which we load from directory.
* Then we make logical version of our 'physical' (image) version.
* We make two dimension array of MazeTitle.
* And also we make copy of our original maze to ensure we got exactly the same one.
*/
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Maze {
    private BufferedImage bitmap;
    private MazeTitle [][] logicalMaze;

    public void GetMazeFromFile(){
        InputStream input = getClass().getResourceAsStream("/Bludiste.bmp");
        if(input == null){
            throw new RuntimeException("File does not exist!");
        }

        try(input){
            bitmap = ImageIO.read(input);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        if(bitmap == null){
            throw new RuntimeException("File could not be read!");
        }
        printMazeSize();
        setupLogicalMaze();
    }

    public void setupLogicalMaze(){
        logicalMaze = new MazeTitle[bitmap.getWidth()][bitmap.getHeight()];
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                MazeTitle title = new MazeTitle();
                title.setUpMazeTitle(x, y);
                int color = bitmap.getRGB(x, y);
                if (color == -16777216) {
                    title.setUpMazeAsSolidBlock();
                } else if (color == -1) {
                    title.setUpMazeAsNotSolidBlock();
                }
                logicalMaze[x][y] = title;
            }
        }

    }

    private void printMazeSize() {
    }

    public BufferedImage createPhysicalMazeFromLogicalState(){
        BufferedImage bmtMaze = new BufferedImage(bitmap.getWidth(), bitmap.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                MazeTitle title = logicalMaze[x][y];
                if(title.isSolidBlock())
                    bmtMaze.setRGB(x,y, Color.BLACK.getRGB());
                else{
                    bmtMaze.setRGB(x,y,Color.WHITE.getRGB());
                }
            }
        }
        return bmtMaze;
    
    }






}


