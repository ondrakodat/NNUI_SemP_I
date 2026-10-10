
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

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
                title.setUpMazeTitle(x,y);
                int color = bitmap.getRGB(x, y);
                System.out.println(color);
                if(color == -16777216){
                    title.setUpMazeAsSolidBlock();
                }
                else if(color == -1){
                    title.setUpMazeAsNotSolidBlock();
                }
                logicalMaze[x][y] = title;
            }
        }

    }

    private void printMazeSize(){
        System.out.println("Width : " + bitmap.getWidth());
        System.out.println("Height : " + bitmap.getHeight());
    }

    public BufferedImage createPhysicalMazeFromLogicalState(){
        BufferedImage bmtMaze = new BufferedImage(bitmap.getWidth(), bitmap.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                MazeTitle title = logicalMaze[x][y];
                if(title.isSolidBlock)
                    bmtMaze.setRGB(x,y, Color.BLACK.getRGB());
                else{
                    bmtMaze.setRGB(x,y,Color.WHITE.getRGB());
                }
            }
        }
        return bmtMaze;
    
    }


    @Getter
    @Setter
    private class MazeTitle{
        private int xCoordinates;
        private int yCoordinates;
        private boolean isSolidBlock;
        private Color color;

        public void setUpMazeTitle(int x, int y){
            this.setXCoordinates(x);
            this.setYCoordinates(y);
        }
        public void setUpMazeAsSolidBlock(){
            this.setColor(Color.BLACK);
            this.setSolidBlock(true);
        }
        public void setUpMazeAsNotSolidBlock(){
            this.setColor(Color.WHITE);
            this.setSolidBlock(false);
        }
    }


}


