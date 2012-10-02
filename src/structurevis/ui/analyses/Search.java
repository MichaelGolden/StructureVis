/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structurevis.ui.analyses;

import java.util.ArrayList;
import java.util.Hashtable;
import structurevis.structures.metadata.NucleotideComposition;

/**
 *
 * @author Michael
 */
public class Search {

    Alphabet alpha = new Alphabet();

    public Search() {
        alpha.putValue("A", "A", 1);
        alpha.putValue("C", "C", 1);
        alpha.putValue("G", "G", 1);
        alpha.putValue("T", "TU", 1);
        alpha.putValue("U", "TU", 1);
        alpha.putValue("Y", "CT", 0.5);
        alpha.putValue("R", "AG", 0.5);
        alpha.putValue("W", "AT", 0.5);
        alpha.putValue("S", "GC", 0.5);
        alpha.putValue("K", "TG", 0.5);
        alpha.putValue("M", "CA", 0.5);
        alpha.putValue("D", "AGT", 1.0 / 3.0);
        alpha.putValue("V", "ACG", 1.0 / 3.0);
        alpha.putValue("H", "ACT", 1.0 / 3.0);
        alpha.putValue("B", "CGT", 1.0 / 3.0);
        alpha.putValue("N", "ACGT", 0.25);
        alpha.putValue("X", "ACGT", 0.25);
        alpha.putValue("-", "-", 1);
    }

    class Alphabet {

        Hashtable<String, String> alphabet = new Hashtable<String, String>();
        Hashtable<String, Double> scores = new Hashtable<String, Double>();

        public String getChars(String code) {
            String chars = alphabet.get(code);
            if (chars == null) {
                return "";
            }
            return chars;
        }

        public double match(String searchString, String s, int startPos) {
            double score = 0;
            double total = 0;

            for (int i = 0; i < searchString.length(); i++) {
                String searchChar = searchString.charAt(i) + "";
                String sChar = s.substring(i + startPos, i + startPos + 1);
                double matchScore = getScore(sChar, searchChar);
                if (charMatches(sChar, searchChar)) {
                    score += matchScore;
                }
                total += matchScore;
            }
            System.out.println(score + "\t" + total);
            return score / total;
        }

        public double match(String searchString, double[][] nucleotideFrequency, int startPos, boolean circular) {
            double score = 0;
            double total = 0;

            for (int i = 0; i < searchString.length(); i++) {
                String searchChar = searchString.charAt(i) + "";

                int pos = i + startPos;
                if(circular)
                {
                    pos %= nucleotideFrequency.length;
                }
                
                double matchScore = nucleotideFrequency[pos][0] * getScore("A", searchChar);
                double subtotal = 0;
                if (charMatches("A", searchChar)) {
                    score += matchScore;
                }
                subtotal += matchScore;

                matchScore = nucleotideFrequency[pos][1] * getScore("C", searchChar);
                if (charMatches("C", searchChar)) {
                    score += matchScore;
                }
                subtotal += matchScore;

                matchScore = nucleotideFrequency[pos][2] * getScore("G", searchChar);
                if (charMatches("G", searchChar)) {
                    score += matchScore;
                }
                subtotal += matchScore;

                matchScore = nucleotideFrequency[pos][3] * getScore("T", searchChar);
                if (charMatches("T", searchChar)) {
                    score += matchScore;
                }
                subtotal += matchScore;
                
                if(subtotal == 0)
                {
                    //subtotal = 1;
                }
                total += subtotal;
            }
            
            return score / total;
        }

        public void putValue(String code, String chars, double score) {
            putChars(code, chars);
            scores.put(code, score);
        }

        private void putChars(String code, String chars) {
            String oldChars = getChars(code);
            for (int i = 0; i < chars.length(); i++) {
                if (!oldChars.contains(chars.charAt(i) + "")) {
                    oldChars += chars.charAt(i) + "";
                }
            }
            alphabet.put(code, oldChars);
        }

        public double getScore(String code1, String code2) {
            Double score1 = scores.get(code1);
            Double score2 = scores.get(code2);
            if (score1 == null) {
                score1 = new Double(0);
            }
            if (score2 == null) {
                score2 = new Double(0);
            }
            return score1 * score2;
        }

        public boolean charMatches(String code1, String code2) {
            String chars1 = getChars(code1);
            String chars2 = getChars(code2);

            for (int i = 0; i < chars1.length(); i++) {
                if (chars2.contains(chars1.charAt(i) + "")) {
                    return true;
                }
            }

            return false;
        }
    }

    public ArrayList<SearchResult> search(String searchString, String s, double cutoff) {
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        for (int i = 0; i < s.length() - searchString.length(); i++) {
            double score = alpha.match(searchString, s, i);
            if (score >= cutoff) {
                SearchResult result = new SearchResult();
                result.searchTerm = searchString;
                result.match = s.substring(i, i + searchString.length());
                result.matchPosition = i;
                result.score = score;
                results.add(result);
            }
        }

        return results;
    }

    public ArrayList<SearchResult> search(String searchString, NucleotideComposition nucleotideComposition, double cutoff, boolean circular) {

        ArrayList<String> searchTerms = getGappedPermutations(searchString, 1);
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        for (int i = 0; i < nucleotideComposition.mappedFrequencyComposition.length; i++) { 
            
            String bestSearchTerm = null;
            double bestScore = -1;
            for (int j = 0;  j < searchTerms.size(); j++) {
                int end = nucleotideComposition.mappedFrequencyComposition.length - searchTerms.get(j).length();
                if(circular)
                {
                    end = nucleotideComposition.mappedFrequencyComposition.length;
                }
                if(i < end)
                {
                    double score = alpha.match(searchTerms.get(j), nucleotideComposition.mappedFrequencyComposition, i, circular);
                    if (score > bestScore) {
                        bestSearchTerm = searchTerms.get(j);
                        bestScore = score;
                    }
                }
            }

            if (bestScore >= cutoff) {
                SearchResult result = new SearchResult();
                result.searchTerm = bestSearchTerm;
                if(i + bestSearchTerm.length() <= nucleotideComposition.mappedFrequencyComposition.length)
                {
                    result.match = nucleotideComposition.consensus.substring(i, i + bestSearchTerm.length());
                }
                else
                {
                    result.match = nucleotideComposition.consensus.substring(i, nucleotideComposition.mappedFrequencyComposition.length);                    
                    result.match += nucleotideComposition.consensus.substring(0, (i + bestSearchTerm.length()) % nucleotideComposition.mappedFrequencyComposition.length);
                }
                result.matchPosition = i;
                result.score = bestScore;
                results.add(result);
            }
        }

        return results;
    }

    public ArrayList<String> getGappedPermutations(String s, int gaps) {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add(s);
        getGappedPermutations(strings, gaps);
        return strings;
    }

    public void getGappedPermutations(ArrayList<String> strings, int gaps) {
        int length = strings.size();
        if (gaps > 0) {
            for (int j = 0; j < length; j++) {
                String s = strings.get(j);
                for (int i = 1; i < s.length() - 1; i++) {
                    strings.add(s.substring(0, i) + "-" + s.substring(i));
                }
            }
            getGappedPermutations(strings, gaps - 1);
        }
    }

    class SearchResult {

        String searchTerm;
        String match;
        int matchPosition;
        double score;

        public String toString() {
            return searchTerm + ":" + match + " (" + matchPosition + "," + score + ")";
        }
    }

    public static void main(String[] args) {
        new Search();
    }
}
