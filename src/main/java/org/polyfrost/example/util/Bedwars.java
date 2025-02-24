package org.polyfrost.example.util;

public class Bedwars {
    private int Bedwarsstar, Bedwarsfk, Bedwarsbb, Bedwarsw, Bedwarsl, Bedwarsfd, Bedwarsbl, Bedwarsws;
    private double Bedwarsfkdr, Bedwarswlr, Bedwarsbblr;

    public Bedwars(int Bedwarsstar, int Bedwarsfk, int Bedwarsbb, int Bedwarsw, int Bedwarsl, int Bedwarsfd, int Bedwarsbl, int Bedwarsws, double Bedwarsfkdr, double Bedwarswlr,
                  double Bedwarsbblr) {
        this.Bedwarsstar = Bedwarsstar;
        this.Bedwarsfk = Bedwarsfk;
        this.Bedwarsbb = Bedwarsbb;
        this.Bedwarsw = Bedwarsw;
        this.Bedwarsl = Bedwarsl;
        this.Bedwarsfd = Bedwarsfd;
        this.Bedwarsbl = Bedwarsbl;
        this.Bedwarsws = Bedwarsws;
        this.Bedwarsfkdr = Bedwarsfkdr;
        this.Bedwarswlr = Bedwarswlr;
        this.Bedwarsbblr = Bedwarsbblr;
    }

    public int getBedwarsStar() { return Bedwarsstar; }
    public int getBedwarsFinalKills() { return Bedwarsfk; }
    public int getBedwarsBedBreaks() { return Bedwarsbb; }
    public int getBedwarsWins() { return Bedwarsw; }
    public int getBedwarsLosses() { return Bedwarsl; }
    public int getBedwarsFinalDeaths() { return Bedwarsfd; }
    public int getBedwarsBedsLost() { return Bedwarsbl; }
    public int getBedwarsWinStreak() { return Bedwarsws; }
    public double getBedwarsFKDR() { return Bedwarsfkdr; }
    public double getBedwarsWLR() { return Bedwarswlr; }
    public double getBedwarsBBLR() { return Bedwarsbblr; }

}
