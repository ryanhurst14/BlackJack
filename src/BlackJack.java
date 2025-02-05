import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {

    private class Card {

        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue(boolean dealer) {
            if ("AJQK".contains(value)) { //A J Q K
                if (value.equals("A")) {
                    if (dealer) {
                        int theoreticalSum = dealerSum + 11;
                        if (theoreticalSum > 21) {
                            return 1;
                        }
                        return 11;
                    } else {
                        int theoreticalSum = playerSum + 11;
                        if (theoreticalSum > 21) {
                            return 1;
                        }
                        return 11;
                    }
                }
                return 10;
            }
            return Integer.parseInt(value); //2-10
        }

        public boolean isAce() {
            return value.equals("A");
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    private class Account {
        int balance;

        Account(int startingBalance) {
            this.balance = startingBalance;
        }

        public String toString() {
            return "Balance: " + balance;
        }

        public int getBalance() {
            return balance;
        }

        public void addBalance(int toAdd) {
            this.balance += toAdd;
        }

        public void minusBalance(int toMinus) {
            this.balance -= toMinus;
        }
    }

    ArrayList<Card> deck;
    Random random = new Random(); //shuffle deck

    //dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    //player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;
    Account playerAccount = new Account(1000);
    int playerBet = 0;

    //window
    int boardWidth = 600;
    int boardHeight = boardWidth;

    int cardWidth = 110; //ratio should 1/1.4
    int cardHeight = 154;

    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {

            super.paintComponent(g);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.setColor(Color.white);
            String playerBalanceMessage = playerAccount.toString();
            g.drawString(playerBalanceMessage, 200, 200);
            //Only draw cards if a bet has been placed
            if (playerBet > 0) {
                try {
                    g.setColor(new Color(53, 101, 77));
                    g.fillRect(150, 150, 300, 100);
                    g.setColor(Color.white);
                    //draw hidden card
                    Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                    if (!stayButton.isEnabled()) {
                        hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                    }
                    g.drawImage(hiddenCardImg, 20, 20, cardWidth, cardHeight, null);

                    //draw dealer's hand
                    for (int i = 0; i < dealerHand.size(); i++) {
                        Card card = dealerHand.get(i);
                        Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                        g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null);
                    }

                    //draw player's hand
                    for (int i = 0; i < playerHand.size(); i++) {
                        Card card = playerHand.get(i);
                        Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                        g.drawImage(cardImg, 20 + (cardWidth + 5)*i, 320, cardWidth, cardHeight, null);
                    }

                    String playerSumMessage = "Player Sum: " + Integer.toString(playerSum);
                    g.drawString(playerSumMessage, 20, 310);
                    playerBalanceMessage = playerAccount.toString();
                    g.drawString(playerBalanceMessage, 20, 520);
                    if (!stayButton.isEnabled()) {
                        dealerSum = reduceDealerAce();
                        playerSum = reducePlayerAce();
                        System.out.println("STAY: ");
                        System.out.println(dealerSum);
                        System.out.println(playerSum);

                        String message = "";
                        if (playerSum > 21) {
                            message = "You Lose!";
                        }
                        else if (dealerSum > 21) {
                            message = "You Win!";
                            playerAccount.addBalance(2*playerBet);
                        }
                        //both you and dealer <= 21
                        else if (playerSum == dealerSum) {
                            message = "Tie!";
                        }
                        else if (playerSum > dealerSum) {
                            message = "You Win!";
                            playerAccount.addBalance(2*playerBet);

                        }
                        else if (playerSum < dealerSum) {
                            message = "You Lose!";

                        }
                        g.drawString(message, 220, 260);
                        String dealerSumMessage = "Dealer Sum: " + Integer.toString(dealerSum);
                        g.drawString(dealerSumMessage, 20, 210);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton doubleButton = new JButton("Double");
    JButton newGameButton = new JButton("New Game");
    JButton betButton = new JButton("Bet 100");

    BlackJack() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        hitButton.setEnabled(false);

        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        stayButton.setEnabled(false);

        doubleButton.setFocusable(false);
        buttonPanel.add(doubleButton);
        doubleButton.setEnabled(false);

        newGameButton.setFocusable(false);
        buttonPanel.add(newGameButton);
        newGameButton.setEnabled(false);

        betButton.setFocusable(false);
        buttonPanel.add(betButton);


        frame.add(buttonPanel, BorderLayout.SOUTH);

        betButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerBet = 100;

                playerAccount.minusBalance(playerBet);
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
                newGameButton.setEnabled(true);
                if (playerAccount.getBalance() >= playerBet) {
                    doubleButton.setEnabled(true);
                }
                betButton.setEnabled(false);
                gamePanel.repaint();
            }
        });

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Card card = deck.removeLast();
                playerSum += card.getValue(false);
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (reducePlayerAce() > 21) { //A + 2 + J --> 1 + 2 + J
                    hitButton.setEnabled(false); 
                }
                //If you bust, then you should lose straight away
                if (playerSum > 21) {
                    stayButton.doClick();
                }
                //If you get 21, you should then stay straight away
                if (playerSum == 21) {
                    stayButton.doClick();
                }
                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);
                doubleButton.setEnabled(false);
                newGameButton.setEnabled(true);

                while (dealerSum < 17) {
                    Card card = deck.removeLast();
                    dealerSum += card.getValue(true);
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }
                gamePanel.repaint();
            }
        });

        doubleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Hit and stay
                playerAccount.minusBalance(playerBet);
                playerBet = 2*playerBet;
                hitButton.doClick();
                stayButton.doClick();
                hitButton.setEnabled(false);
                doubleButton.setEnabled(false);
                gamePanel.repaint();
            }
        });
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                stayButton.setEnabled(true);
//                hitButton.setEnabled(true);
//                doubleButton.setEnabled(true);
//
                playerBet = 0;
                stayButton.setEnabled(false);
                hitButton.setEnabled(false);
                doubleButton.setEnabled(false);
                newGameButton.setEnabled(false);
                betButton.setEnabled(true);
                startGame();
                gamePanel.repaint();



            }
        });
        gamePanel.repaint();
    }

    public void startGame() {
        //deck
        buildDeck();
        shuffleDeck();

        //dealer
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.removeLast();
        dealerSum += hiddenCard.getValue(true);
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.removeLast();
        dealerSum += card.getValue(true);
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        System.out.println("DEALER:");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);


        //player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.removeLast();
            playerSum += card.getValue(false);
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        System.out.println("PLAYER: ");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAceCount);
    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (String type : types) {
            for (String value : values) {
                Card card = new Card(value, type);
                deck.add(card);
                deck.add(card);

            }
        }

        System.out.println("BUILD DECK:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("AFTER SHUFFLE");
        System.out.println(deck);
    }

    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }
}
