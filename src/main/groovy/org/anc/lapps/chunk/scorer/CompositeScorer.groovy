package org.anc.lapps.chunk.scorer

/**
 * Created by krim on 8/16/2016.
 */
class CompositeScorer implements WindowScorerI {

    private List<WindowScorerI> scorers;

    /**
     * this is default, uniform distribution lambda
     * to customize these lambdas, user should provide a parameter
     */
    public void useDefaultScorers() {
        removeAll();
        add( new TermRecallScorer(), 0.25d );
        add( new MatchRecallScorer(), 0.25d );
        add( new BrevityScorer(), 0.25d );
        add( new OffsetScorer(), 0.25d );
    }

    public void removeAll() {
        scorers.clear();
    }

    public void add(WindowScorerI scorer, Double lambda ) {
        scorers.add(new WeightedScorer( scorer , lambda));
    }

    @Override
    public double scoreWindow(int begin, int end, int matchesFound,
                              int totalMatches, int keytermsFound, int totalKeyterms, int textSize) {
        double result = 0.0d;
        for ( WindowScorerI scorer : scorers ) {
            double score = scorer.scoreWindow( begin , end , matchesFound , totalMatches , keytermsFound , totalKeyterms , textSize );
            if ( score > 1.0d || score < 0.0d )
                System.out.println( scorer.getClass().getSimpleName() + " OUT OF BOUNDS: " + score );
            result += score;
        }
        return result;
    }
    private class WeightedScorer implements WindowScorerI {
        Double lambda;
        WindowScorerI scorer;
        public WeightedScorer(WindowScorerI scorer , Double lambda ) {
            this.scorer = scorer;
            this.lambda = lambda;
        }

        @Override
        public double scoreWindow(int begin, int end, int matchesFound,
                                  int totalMatches, int keytermsFound, int totalKeyterms,
                                  int textSize) {
            return lambda * scorer.scoreWindow( begin , end , matchesFound , totalMatches , keytermsFound , totalKeyterms , textSize );
        }

    }
}
