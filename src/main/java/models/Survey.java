package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Survey {

    private static int count;

    private int ID;

    //lines that will be sent to server when needed
    private String surveyLine;
    private String statisticsLine;

    //line that will be sent from server to users that will do survey
    private HashMap<String, ArrayList<String>> questions = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> votes;
    private ArrayList<String> usersList;

    private int amountOfUsersVoted;

    public Survey(String survey) {
        storeSurvey(survey);
    }

    public Survey(String survey, String users){
        storeSurvey(survey);

        String[] line1 = users.split(";");
        usersList = new ArrayList<>(Arrays.asList(line1));

        surveyLine = survey;
        count++;
    }
    public void storeSurvey(String survey) {
        //Split in questions
        String[] line = survey.split("/");

            for (String questionLine : line
            ) {
                ArrayList<String> answers = new ArrayList<>();

                //Split the question from the answers
                String[] line2 = questionLine.split(";");

                String question = line2[0];
                String multipleAnswers = line2[1];

                String[] line3 = multipleAnswers.split(",");


                    String correctAnswer = line3[0];
                    String answer2 = line3[1];
                    String answer3 = line3[2];

                    answers.add(correctAnswer);
                    answers.add(answer2);
                    answers.add(answer3);

                    questions.put(question, answers);
                }

            setResultsToZero(questions);
            amountOfUsersVoted = 0;
        }

    //based on the number of questions => that many keys will be created in the results map
    private void setResultsToZero(HashMap<String, ArrayList<String>> questions){
        votes = new HashMap<>();

        questions.forEach((statement, answers) -> {
            //array destined for storing the votes for each answer
            //each array will have 3 elements, because each question has 3 answers
            //we add only zeros to the array because in future we will increase the value based on how many people voted for this answer
            ArrayList<Integer> emptyArray = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                emptyArray.add(0);
            }
            votes.put(statement, emptyArray);
        });
    }

    public void checkAnswers(String serverLine) {

        HashMap<String, ArrayList<Integer>> localResults = new HashMap<>();

        //answers list, each separated by %
        String[] line = serverLine.split("%");

        for (int answerCounter = 0; answerCounter < line.length ; answerCounter++) {
                String correctAnswer = line[answerCounter];

                //retrieving question
                Object firstKey = votes.keySet().toArray()[answerCounter];
                //retrieving answers
                ArrayList<Integer> valueForFirstKey = votes.get(firstKey);

                String question = firstKey.toString();

                int firstAnswerVotes = valueForFirstKey.get(0);
                int secondAnswerVotes = valueForFirstKey.get(1);
                int thirdAnswerVotes = valueForFirstKey.get(2);
//////
                if (correctAnswer.equalsIgnoreCase("1")) {
                    firstAnswerVotes++;
                    valueForFirstKey.set(0, firstAnswerVotes);
                    localResults.put(question, valueForFirstKey);
                } else if (correctAnswer.equalsIgnoreCase("2")) {
                    secondAnswerVotes++;
                    valueForFirstKey.set(1, secondAnswerVotes);
                    localResults.put(question, valueForFirstKey);
                } else if (correctAnswer.equalsIgnoreCase("3")) {
                    thirdAnswerVotes++;
                    valueForFirstKey.set(2, thirdAnswerVotes);
                    localResults.put(question, valueForFirstKey);
                }
            }

        votes = localResults;
        amountOfUsersVoted++;
    }

    public String statisticsMessage(){
        StringBuilder stb = new StringBuilder();

        questions.forEach((question, answers)-> {
            stb.append(question).append(";");

            for (int i = 0; i < answers.size(); i++) {
                    stb.append(answers.get(i)).append(",");
            }
        votes.forEach((key, votes) -> {

         if (key.equals(question)) {
             for (int i = 0; i < votes.size(); i++) {
                 if (i == votes.size() - 1) {
                     stb.append(votes.get(i));
                 } else {
                     stb.append(votes.get(i)).append(",");
                 }
             }

             stb.append("/");
         }
        });
        });

        statisticsLine = stb.toString();

        return statisticsLine;
    }


    @Override
    public String toString() {

        questions.forEach((question, answers ) ->{
            System.out.println(" ");
            System.out.println(question);

            int answerCounter = 1;
            for (String answer : answers
            ) {
                System.out.println(answerCounter + ": " + answer);
                answerCounter++;
            }

        });

        return questions.size() + " questions to answer!";
    }

    public String getServerMessage() {
        return surveyLine;
    }

    public ArrayList<String> getUsers() {
        return usersList;
    }

    public int getAmountOfUsersVoted() {
        return amountOfUsersVoted;
    }

}
