package francoispog.samp.quiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private TextView questionAnswer;

    enum State {
        LOADING,
        BEFORE_ANSWER,
        AFTER_ANSWER,
    }

    protected State state;
    protected Question currentQuestion;
    protected AsyncQuestionFetcher questionFetcher;
    protected Button nextButton;
    private static final int NUMBER_OF_BUTTONS = 4;
    private Button[] buttons;
    private TextView questionLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionLabel = (TextView) findViewById(R.id.question_label);
        questionAnswer = (TextView) findViewById(R.id.question_good_answer);

        nextButton = (Button) findViewById(R.id.button_next);
        nextButton.setOnClickListener(e -> nextQuestion());

        buttons = new Button[NUMBER_OF_BUTTONS];
        for (int i = 0; i < 4; ++i) {
            int buttonId = getResources().getIdentifier("button_answer_" + i, "id", getPackageName());
            buttons[i] = (Button) findViewById(buttonId);
            int finalI = i;
            buttons[i].setOnClickListener(e -> this.onClickAnswer(finalI));
        }


        nextQuestion();
    }

    protected void onClickAnswer(int buttonNumber) {
        if (this.state != State.BEFORE_ANSWER) {
            return;
        }

        String toastText = "Mauvaise réponse...";
        if (buttonNumber == this.currentQuestion.getGoodAnswer()) {
            toastText = "Bonne réponse !";
        }
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

        this.setState(State.AFTER_ANSWER);
    }

    protected void nextQuestion() {
        this.setState(State.LOADING);

        new AsyncQuestionFetcher(object -> {
            this.currentQuestion = (Question) object;
            this.setState(State.BEFORE_ANSWER);
        }).execute();

    }

    protected void setState(State state) {
        this.state = state;
        this.render();
    }

    protected void render() {
        questionAnswer.setVisibility(View.VISIBLE);
        questionAnswer.setVisibility(View.INVISIBLE);

        if (this.state == State.LOADING) {
            questionLabel.setText(R.string.loading);
        } else {
            questionLabel.setText(currentQuestion.getWording());
            if (this.state == State.AFTER_ANSWER) {
                questionAnswer.setText(currentQuestion.getAnswers().get(currentQuestion.getGoodAnswer()));
                questionAnswer.setVisibility(View.VISIBLE);
            }
        }


        for (int i = 0; i < NUMBER_OF_BUTTONS; ++i) {
            Button button = buttons[i];

            button.setEnabled(false);

            if (this.state == State.LOADING) {
                button.setText(R.string.loading);
            } else {
                if (this.state == State.BEFORE_ANSWER) {
                    button.setEnabled(true);
                }
                button.setText(currentQuestion.getAnswers().get(i));
            }


        }

        nextButton.setEnabled(this.state == State.AFTER_ANSWER);
    }
}

class Question {

    private final String wording;
    private final List<String> answers;
    private final int goodAnswer;

    public String getWording() {
        return wording;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public int getGoodAnswer() {
        return goodAnswer;
    }


    public Question(String wording, int goodAnswer, List<String> answers) {
        this.wording = wording;
        this.goodAnswer = goodAnswer;
        this.answers = answers;
    }

    @NonNull
    @Override
    public String toString() {
        return "Question{" +
                "wording='" + wording + '\'' +
                ", answers=" + answers +
                ", goodAnswer=" + goodAnswer +
                '}';
    }
}

abstract class AsyncTaskCallback<Params, Progress, Result> extends android.os.AsyncTask<Params, Progress, Result> {
    protected AsyncTaskCallbackFunction callback;

    public AsyncTaskCallback(AsyncTaskCallbackFunction callback) {
        super();
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(Result object) {
        super.onPostExecute(object);
        if (this.callback != null) {
            this.callback.onTaskFinished(object);
        }
    }

    interface AsyncTaskCallbackFunction {
        public void onTaskFinished(Object object);
    }
}

class AsyncQuestionFetcher extends AsyncTaskCallback<Void, Void, Question> {


    public AsyncQuestionFetcher(AsyncTaskCallbackFunction callback) {
        super(callback);
    }

    @Override
    protected Question doInBackground(Void... voids) {
        BufferedReader bufferedReader = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("https://apothiquiz.univ-fcomte.fr/api/v1/question/1");
            urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


                JSONObject data = new JSONObject(bufferedReader.readLine());
                String wording = data.getString("wording");
                List<String> answers = new ArrayList<>();

                JSONArray answersData = data.getJSONArray("answers");
                for (int i = 0; i < answersData.length(); i++) {
                    answers.add(answersData.getString(i));
                }

                int goodAnswer = data.getInt("goodAnswer");

                return new Question(wording, goodAnswer, answers);
            } else {
                System.out.println("not ok");
                return null;
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


}