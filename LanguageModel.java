import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		 char ch;
		 In in = new In(fileName);
		 String window = "";

		  for (int i = 0; i < this.windowLength; i++) {
		  	window = window + in.readChar();
		  }
		  while (!in.isEmpty()){
		  	List pro = CharDataMap.get(window);
		  	ch = in.readChar();

		  	if(pro == null){
		  		pro = new List();
		  		CharDataMap.put(window,pro);
		  	}
		  	pro.update(ch);
		  	window = window.substring(1)+ch;
		  }
		  for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		int size = probs.getSize();
		int numChars =0;

		for(int i = 0; i<size; i++){
			numChars = numChars + probs.get(i).count;
		}
		CharData first = probs.getFirst();
		Double firstP = (double)first.count / numChars;
		first.p = firstP;
		first.cp = firstP;
		CharData prev = first;
		for(int j =1; j<size; j++){
			CharData current = probs.get(j);
			double result = (double) current.count / numChars;
			current.p = result;
			current.cp = prev.cp + result;
			prev = current;
		}
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		double random = randomGenerator.nextDouble();
		for (int i =0; i< probs.getSize(); i++){
			CharData ch = probs.get(i);
			if(random < ch.cp){
				return ch.chr;
			}
		}
		return probs.get(probs.getSize()-1).chr;	
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) 
            return initialText;
        String textGenerated = initialText.substring(initialText.length() - windowLength);
        String window = textGenerated;
        for (int i = 0; i < textLength; i++) {
            List probs = CharDataMap.get(window);
             if (probs != null) {
                char ch = getRandomChar(probs);
                textGenerated = textGenerated + ch;
                window = textGenerated.substring(textGenerated.length() - windowLength);
            }
            else{
            	return textGenerated;
            }
        }
        return textGenerated;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
