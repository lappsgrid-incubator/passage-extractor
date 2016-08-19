package org.anc.lapps.chunk.scorer

import org.anc.lapps.chunk.window.Window

/**
 * Created by krim on 8/16/2016.
 */
class CompositeScorer implements WindowScorerI {

    private List<WindowScorerI> scorers;
    def nameSpace = ['termrecall' : TermRecallScorer.class,
                     'matchrecall' : MatchRecallScorer.class,
                     'brevity' : BrevityScorer.class,
                     'offset' : OffsetScorer.class]

    CompositeScorer() {
        scorers = []
    }

    @Override
    String toString() {
        return scorers.toString()
    }
/**
     * initiates an instance with specific configuration for each scorers
     * @param configuration - needs to be formatted
     *  "scorer1Name : weight, scorer2Name : weight, ..."
     *  name should be one of ['termrecall', 'matchrecall', 'brevity', 'offset'] all lower cases
     *  sum of weight should be 1
     *  (does not have to be using all 4 scorers)
     */
    CompositeScorer(String configuration) {
        this()
        configuration.split("\\s*,\\s*").each {
            scorerString ->
                // TODO: 2016-08-17 18:51:40EDT add error handling for unacceptable names
                def name = scorerString.split("\\s*:\\s*")[0]
                def weightString = scorerString.split("\\s*:\\s*")[1]
                WindowScorerI scorer = nameSpace[name].newInstance()
                Double weight = Double.parseDouble(weightString)
                this.add(scorer, weight)
        }

    }

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
        scorers.add(new WeightedScorer(scorer , lambda));
    }

    @Override
    public double scoreWindow(Window window, Window document) {
        double result = 0.0d;
        for ( WindowScorerI scorer : scorers ) {
            double score = scorer.scoreWindow(window, document);
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
        public double scoreWindow(Window window, Window document) {
            return lambda * scorer.scoreWindow(window, document);
        }

        @Override
        String toString() {
            return this.scorer.class.getName() + " : " + this.lambda
        }
    }
}
