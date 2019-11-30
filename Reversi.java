import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Player {
    public final static int type_human = 0;
    public final static int type_computer = 1;
    private int color;
    private int type;

    Player(int c, int t) {
        if(c == Stone.black || c == Stone.white) { this.color = c; }
        else {
            System.out.println("プレイヤーの石は黒か白でなければいけません:" + c);
            System.exit(0);
        }
        if(t == type_human || t == type_computer) { this.type = t; }
        else {
            System.out.println("プレイヤーは人間かコンピュータでなければいけません:" + t);
            System.exit(0);
        }
    }

    int getColor() { return color; }

    int getType() { return type; }

    Point tactics(Board bd) {
        /* 左上優先の戦略 */
        if(color == Stone.black) {
            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    if(bd.eval_black[i][j] > 0) {
                        return (new Point(i, j));
                    }
                }
            }
        }
        else if(color == Stone.white) {
            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    if(bd.eval_white[i][j] > 0) {
                        return (new Point(i, j));
                    }
                }
            }
        }
        /* 配置可能な場所がない場合 */
        return (new Point(-1, -1));
    }

    Point nextMove(Board bd, Point p) {
        if(this.type == type_human) { return p; }
        else if(this.type == type_computer) { return tactics(bd); }
        /* 通常はあり得ない */
        return (new Point(-1, -1));
    }
}


class Stone {
    public final static int black = 1;
    public final static int white = 2;
    private int obverse;

    Stone(){
        obverse = 0;
    }

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

    /* 表面の色を設定 */
    void setObverse(int color) {
        if(color == black || color == white) { obverse = color; }
        else { System.out.println("黒か白でなければいけません"); }
    }

    /* 表面の色を取得 */
    int getObverse() { return obverse; }

    /* 石を反転 */
    void doReverse() {
        if(this.obverse == black) { this.obverse = white; }
        else if(this.obverse == white) { this.obverse = black; }
    }
}


class Board {
    private Stone[][] stone = new Stone[8][8];
    public int num_grid_black; /* 黒石を配置できるマス目の数 */
    public int num_grid_white; /* 白石を配置できるマス目の数 */
    private Point[] direction = new Point[8];
    public int[][] eval_black = new int[8][8];
    public int[][] eval_white = new int[8][8];

    Board() {
        /* 盤面の初期化 */
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                stone[i][j] = new Stone();
            }
        }
        stone[3][3].setObverse(Stone.black);
        stone[3][4].setObverse(Stone.white);
        stone[4][3].setObverse(Stone.white);
        stone[4][4].setObverse(Stone.black);
        /* 方向ベクトルの生成 */
        direction[0] = new Point(1, 0);
        direction[1] = new Point(1, 1);
        direction[2] = new Point(0, 1);
        direction[3] = new Point(-1, 1);
        direction[4] = new Point(-1, 0);
        direction[5] = new Point(-1, -1);
        direction[6] = new Point(0, -1);
        direction[7] = new Point(1, -1);
        /* 盤面の初期評価 */
        evaluateBoard();
        printEval();
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

    /* 盤面内か判定 */
    boolean isOnBoard(int x, int y) {
        if(x < 0 || 7 < x || y < 0 || 7 < y) { return false; }
        else { return true; }
    }

    /* 盤面(x, y)に石sを配置 */
    void setStone(int x, int y, int s) {
        stone[x][y].setObverse(s);
    }

    /* 石を数える */    
    int countStone(int s) {
        int cnt = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(stone[i][j].getObverse() == s) { cnt++; }
            }
        }
        return cnt;
    }

    /* 盤面(x, y)から方向dに向かって石を順に取得 */
    ArrayList<Integer> getLine(int x, int y, Point d) {
        ArrayList<Integer> line = new ArrayList<Integer>();
        int cx = x + d.x;
        int cy = y + d.y;
        while(isOnBoard(cx, cy) && stone[cx][cy].getObverse() != 0) {
            line.add(stone[cx][cy].getObverse());
            cx += d.x;
            cy += d.y;
        }
        return line;
    }

    /* 盤面(x, y)に石を置いた場合に反転できる石の数を数える */
    int countReverseStone(int x, int y, int s) {
        if(stone[x][y].getObverse() != 0) return -1;
        /* 8方向をチェック */
        int cnt = 0;
        for(int d = 0; d < 8; d++) {
            ArrayList<Integer> line = new ArrayList<Integer>();
            line = getLine(x, y, direction[d]);
            int n = 0;
            while(n < line.size() && line.get(n) != s) { n++; }
            if(1 <= n && n < line.size()) { cnt += n; }
        }
        return cnt;
    }

    /* 盤面を評価 */
    void evaluateBoard() {
        num_grid_black = 0;
        num_grid_white = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                eval_black[i][j] = countReverseStone(i, j, Stone.black);
                if(eval_black[i][j] > 0) { num_grid_black++; }
                eval_white[i][j] = countReverseStone(i, j, Stone.white);
                if(eval_white[i][j] > 0) { num_grid_white++; }
            }
        }
    }

    /* 盤面をコンソールに表示(テスト用) */
    void printBoard() {
        System.out.println("Board:");
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                System.out.printf("%2d ", stone[j][i].getObverse());
            }
            System.out.println();
        }
    }

    /* 盤面の評価結果をコンソールに表示(テスト用) */
    void printEval() {
        System.out.println("Black(1):");
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                System.out.printf("%2d ", eval_black[j][i]);
            }
            System.out.println("");
        }
        System.out.println("White(2):");
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                System.out.printf("%2d ", eval_white[j][i]);
            }
            System.out.println();
        }
    }

    /* 盤面(x, y)に石sを配置し該当する石を反転 */
    void setStoneAndReverse(int x, int y, int s) {
        setStone(x, y, s);
        /* 8方向をチェック */
        for(int d = 0; d < 8; d++) {
            ArrayList<Integer> line = new ArrayList<Integer>();
            line = getLine(x, y, direction[d]);
            int n = 0;
            while(n < line.size() && line.get(n) != s) { n++; }
            if(1 <= n && n < line.size()) {
                int cx = x + direction[d].x;
                int cy = y + direction[d].y;
                for(int i = 0; i < n; i++) {
                    stone[cx][cy].doReverse();
                    cx += direction[d].x;
                    cy += direction[d].y;
                }
            }
        }
        evaluateBoard();
    }
}


public class Reversi extends JPanel{
    public final static int UNIT_SIZE = 80;
    private Board board = new Board();
    private int turn;
    private Player[] player = new Player[2];

    public Reversi() {
        setPreferredSize(new Dimension(UNIT_SIZE*10, UNIT_SIZE*10));
        addMouseListener(new MouseProc());
        player[0] = new Player(Stone.black, Player.type_human);
        player[1] = new Player(Stone.white, Player.type_computer);
        turn = Stone.black;
    }

    public void paintComponent(Graphics g) {
        board.paint(g, UNIT_SIZE);   
        String msg1 = "";
        if(turn == Stone.black) { msg1 = "黒の番です"; }
        else { msg1 = "白の番です"; }
        if(player[turn-1].getType() == Player.type_computer) { msg1 += "(考えています)"; }
        String msg2 = "[黒:" + board.countStone(Stone.black) + ", 白:" + board.countStone(Stone.white) + "]";
        g.setColor(Color.white);
        g.drawString(msg1, UNIT_SIZE/2, UNIT_SIZE/2);
        g.drawString(msg2, UNIT_SIZE/2, 19*UNIT_SIZE/2);
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

    void EndMessageDialog() {
        String str;
        int black = board.countStone(Stone.black);
        int white = board.countStone(Stone.white);
        if(black > white) {
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

    void MessageDialog(String str) {
        JOptionPane.showMessageDialog(this, str, "情報", JOptionPane.INFORMATION_MESSAGE);
    }

    void changeTurn() {
        if(turn == Stone.black) { turn = Stone.white; }
        else { turn = Stone.black; }
    }


    class MouseProc extends MouseAdapter {
        public void mouseClicked(MouseEvent me) {
            Point point = me.getPoint();
            Point gp = new Point();
            /* マス目を決定 */
            gp.x = point.x/UNIT_SIZE - 1;
            gp.y = point.y/UNIT_SIZE - 1;
            /* 盤面内か判定 */
            if(!board.isOnBoard(gp.x, gp.y)) { return; }
            /* 一時的にマウスイベントを止める */
            removeMouseListener(this);
            /* 人間の手番 */
            if(player[turn-1].getType() == Player.type_human) {
                if((player[turn-1].getColor() == Stone.black && board.num_grid_black == 0)
                    || (player[turn-1].getColor() == Stone.white && board.num_grid_white == 0)) {
                    MessageDialog("あなたはパスです");
                    changeTurn();
                    repaint();
                }
                else if ((player[turn-1].getColor() == Stone.black && board.eval_black[gp.x][gp.y] > 0)
                    || (player[turn-1].getColor() == Stone.white && board.eval_white[gp.x][gp.y] > 0)) {
                    Point nm = player[turn-1].nextMove(board, gp);
                    board.setStoneAndReverse(nm.x, nm.y, player[turn-1].getColor());
                    changeTurn();
                    repaint();
                    board.printBoard();
                    board.printEval();
                    /* 終了判定 */
                    if(board.num_grid_black == 0 && board.num_grid_white == 0) {
                        EndMessageDialog();
                    }
                }
                /* 人間対人間 */
                if(player[turn-1].getType() == Player.type_human) {
                    addMouseListener(this);
                }
            }
            /* コンピュータの手番 */
            if(player[turn-1].getType() == Player.type_computer) {
                Thread th = new TacticsThread();
                th.start();
            }
        }
    }


    class TacticsThread extends Thread {
        public void run() {
            try {
                Thread.sleep(2000);
                Point nm = player[turn-1].nextMove(board, new Point(-1, -1));
                if(nm.x == -1 && nm.y == -1) {
                    MessageDialog("相手はパスです");
                }
                else {
                    board.setStoneAndReverse(nm.x, nm.y, player[turn-1].getColor());
                }
                changeTurn();
                repaint();
                board.printBoard();
                board.printEval();
                addMouseListener(new MouseProc());
                /* 終了判定 */
                if(board.num_grid_black == 0 && board.num_grid_white == 0) {
                    EndMessageDialog();
                }
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}
