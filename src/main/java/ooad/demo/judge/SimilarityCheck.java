package ooad.demo.judge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xm.Similarity;

@Service
public class SimilarityCheck {

    public double checkTwoCodes(String code_1, String code_2){
        double morphoSimilarityResult
                = Similarity.morphoSimilarity(code_1, code_2);
        double standEditDistanceResult
                = Similarity.standardEditDistanceSimilarity(code_1,code_2);
        return morphoSimilarityResult + 10 * standEditDistanceResult;
    }

}
