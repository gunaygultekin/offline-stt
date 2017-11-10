package project.gunay.demoSTT.stt;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.cmu.sphinx.result.WordResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Created by Gunay Gultekin on 6/14/2017.
 */
public class Sphinx4 {
    public static final String wav_file = "/static/sphinx4project/sample-files/10001-90210-01803.wav";

    public static String runTool(MultipartFile uploadedFile) throws Exception{
        System.out.println("******************************************************************************************");
        System.out.println("Loading Sphinx models...");
        Configuration configuration = new Configuration();

        // Load model from the jar
        configuration
                .setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us"); //set dictionary

        // You can also load model from folder
        // configuration.setAcousticModelPath("file:en-us");

        configuration
                .setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict"); //set language model
        configuration
                .setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
                configuration);
        InputStream stream = uploadedFile.getInputStream();
        stream.skip(44);

        // Simple recognition with generic model
        recognizer.startRecognition(stream);
        SpeechResult result;
        while ((result = recognizer.getResult()) != null) {

            System.out.format("Hypothesis: %s\n", result.getHypothesis());

            System.out.println("List of recognized words and their times:");
            for (WordResult r : result.getWords()) {
                System.out.println(r);
            }

            System.out.println("Best 3 hypothesis:");
            for (String s : result.getNbest(3))
                System.out.println(s);

        }
        recognizer.stopRecognition();

        // Live adaptation to speaker with speaker profiles

        stream = uploadedFile.getInputStream();
        stream.skip(44);

        // Stats class is used to collect speaker-specific data
        Stats stats = recognizer.createStats(1);
        recognizer.startRecognition(stream);
        while ((result = recognizer.getResult()) != null) {
            stats.collect(result);
        }
        recognizer.stopRecognition();

        // Transform represents the speech profile
        Transform transform = stats.createTransform();
        recognizer.setTransform(transform);

        // Decode again with updated transform
        stream = uploadedFile.getInputStream();
        stream.skip(44);
        recognizer.startRecognition(stream);
        StringBuilder sb =  new StringBuilder();
        while ((result = recognizer.getResult()) != null) {
            sb.append(result.getHypothesis() + " ");
            System.out.format("Hypothesis: %s\n", result.getHypothesis());
        }
        recognizer.stopRecognition();

        System.out.println("Sphinx4 result is : " + sb.toString());
        System.out.println("******************************************************************************************");
        return sb.toString();
    }
}
