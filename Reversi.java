import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Stone {
    public final static int black = 1;
    public final static int white = 2;
    private int obverse;

    Stone(){
        obverse = 0;
    }

    /* 表面の色を設定 */
    void setObverse(int color) {
        if(color == black || color == white) { obverse = color; }
        else { System.out.println("黒か白でなければいけません"); }
    }

    int getObverse() { return obverse; }

    void paint(Graphics g, Point p, int rad) {
        if(obverse == black) {
            g.setColor(Color.black);
            g.fillOval(p.x-rad, p.y-rad, rad*2, rad*2);
        }
        else if(obverse == white) {
            g.setColor(Color.white);
            g.fillOval(p.x-rad, p.y-rad, rad*2, rad*2);
        }
    }
}


class Board {
    private Stone[][] stone = new Stone[8][8];

    Board() {
        /* 盤面の初期化 */
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                stone[i][j] = new Stone();
            }
        }
        stone[3][3].setObverse(1);
        stone[3][4].setObverse(2);
        stone[4][3].setObverse(2);
        stone[4][4].setObverse(1);
    }

    void paint(Graphics g, int unit_size) {
        /* 背景 */
        g.setColor(Color.black);
        g.fillRect(0, 0, unit_size*10, unit_size*10);
        /* 盤面 */
        g.setColor(new Color(0, 85, 0));
        g.fillRect(unit_size, unit_size, unit_size*8, unit_size*8);
        /* 横線 */
        g.setColor(Color.black);
        for(int i = 0; i < 9; i++) {
            g.drawLine(unit_size, unit_size*(i+1), unit_size*9, unit_size*(i+1));
        }
        /* 縦線 */
        for(int i = 0; i < 9; i++) {
            g.drawLine(unit_size*(i+1), unit_size, unit_size*(i+1), unit_size*9);
        }
        /* 目印 */
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                g.fillRect(unit_size*(3+4*i)-5, unit_size*(3+4*j)-5, 10, 10);
            }
        }

        /* 盤面の更新 */
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                /* 中心 */
                Point center = new Point((int)(unit_size*(i+1.5)), (int)(unit_size*(j+1.5)));
                /* 半径 */
                int radius = (int)(unit_size*0.5*0.8);
                stone[i][j].paint(g, center, radius);
            }
        }
    }

    void setStone(int x, int y, int s) {
        stone[x][y].setObverse(s);
    }

    /* すべてのマス目に配置されているか判定 */
    boolean checkPlaced() {
        boolean flag = true;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(stone[i][j].getObverse() == 0) { 
                    flag = false;
                    break;
                }
            if(!flag) { break; }
            }
        }    
        return flag;
    }

    /* 石を数える */    
    int count() {
        int black = 0;
        int white = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(stone[i][j].getObverse() == 1) { black++; }
                else if(stone[i][j].getObverse() == 2) { white++; }
            }
        }
        if(black+white == 64) { return black; }
        else { return -1; } /* Error:通常は入らない */
    }
}


public class Reversi extends JPanel{
    public final static int UNIT_SIZE = 80;
    private Board board = new Board();

    public Reversi() {
        setPreferredSize(new Dimension(UNIT_SIZE*10, UNIT_SIZE*10));
        addMouseListener(new MouseProc());
    }

    public void paintComponent(Graphics g) {
        board.paint(g, UNIT_SIZE);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(new Reversi());
        f.pack();
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    void MessageDialog(int black, int white) {
        String str;
        if(black < 0) {
            str = "ERROR";
        }
        else if(black > white) {
            str = "[黒:" + black + ",白:" + white + "]で黒の勝ち";
        }
        else if(black < white) {
            str = "[黒:" + black + ",白:" + white + "]で白の勝ち";
        }
        else {
            str = "[黒:" + black + ",白:" + white + "]で引き分け";
        }
        JOptionPane.showMessageDialog(this, str, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }


    class MouseProc extends MouseAdapter {
        public void mouseClicked(MouseEvent me) {
            /* マス目を決定 */
            Point point = me.getPoint();
            int x = point.x/UNIT_SIZE - 1;
            int y = point.y/UNIT_SIZE - 1;
            /* 色を決定 */
            int btn = me.getButton();
            int s = -1;
            if(btn == MouseEvent.BUTTON1) { s = 1; }
            else if(btn == MouseEvent.BUTTON3) { s = 2; }
            /* 盤面内か判定 */
            if((0 <= x && x < 8) && (0 <= y && y < 8) && s != -1) {
                /* マス目に石を配置 */
                board.setStone(x, y, s);
                repaint();
            }
            /* 終了判定 */
            if(board.checkPlaced()) {
                int black = board.count();
                int white = 64 - black;
                MessageDialog(black, white);
            }
        }
    }
}