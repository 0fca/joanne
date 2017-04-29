/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery;

/**
 *
 * @author Obsidiam
 */
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ScreenCenter {
    public int getX(int x) {
        int divx = x / 2;
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension dim = kit.getScreenSize();
        int width = dim.width / 2;
        x = width - divx;
        return x;
    }

    public int getY(int y) {
        int divy = y / 2;
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension dim = kit.getScreenSize();
        int height = dim.height / 2;
        y = height - divy;
        return y;
    }

    public int sizeX() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension dim = kit.getScreenSize();
        int x = dim.width;
        return x;
    }

    public int sizeY() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension dim = kit.getScreenSize();
        int y = dim.height;
        return y;
    }

    public Dimension screenSize() {
        Dimension kit = Toolkit.getDefaultToolkit().getScreenSize();
        return kit;
    }

    public int screenResolution() {
        int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        return resolution;
    }

    public Object clipboard() throws UnsupportedFlavorException, IOException {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        DataFlavor d = DataFlavor.getTextPlainUnicodeFlavor();
        Transferable o = c.getContents(this);
        return o;
    }
}
