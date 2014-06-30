package org.tomar.tomar24;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.ToMar.Utils.Functions;
import org.ToMar.Utils.tmEvaluator;
import org.tomar.tomar24.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity
{
	static final int MAXPUZZLES = 30;
	static final int NUMBERS = 4;
	static final int TARGET = 24;
	static final String GAMENAME = "ToMar24";
	static final String BEST = "best";
	private ArrayList<Object> elementList;
	ArrayList<String> puzzleStrings;
	Puzzle puzzle;
	TextView message;
	TextView guess;
	TextView score;
	TextView scoreLabel;
	TextView best;
	TextView bestLabel;
	int points = 0;
	Operator[] operators = new Operator[6];
	Button[] puzzleButtons = new Button[NUMBERS];
	Button[] operButtons = new Button[6];
	Button submitButton;
	Button giveUpButton;
	Button backButton;
	Button clearButton;
	boolean gameOver;
	SharedPreferences gamePrefs;
	int bestScore = 0;
	boolean digitNeeded = true;
	String puzzleSolution;

	static void log(String s)
	{
		System.out.println(Functions.getDateTimeStamp() + ": " + s);
	}
	protected void onDestroy()
	{
	    checkHighScore();
	    super.onDestroy();
	}
	public void onBackPressed()
	{
		message.setText("Back button disabled: use Home");	
	}
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gamePrefs = getSharedPreferences(GAMENAME, 0);
        String s;
        s = gamePrefs.getString(BEST, "");
        if (!s.isEmpty())
        {
        	bestScore = Integer.parseInt(s);
        }
        gameOver = false;
        puzzleStrings = new ArrayList<>();
        try
        {
            InputStream is = this.getClass().getResourceAsStream("ToMar24.txt");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is)))
            {
                while   (true)
                {
                    s = br.readLine();
                    if  (s == null)
                    {
                        break;
                    }
                    puzzleStrings.add(s);
                }
            }
        }
        catch   (Exception e)
        {
        	log("Exception reading ToMar24 input file: " + e);
        }
		message = (TextView) findViewById(R.id.messageText);
		guess = (TextView) findViewById(R.id.guessText);
		guess.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
		message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		score = (TextView) findViewById(R.id.score_value);
		score.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		scoreLabel = (TextView) findViewById(R.id.score_label);
		scoreLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		best = (TextView) findViewById(R.id.best_value);
		best.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		bestLabel = (TextView) findViewById(R.id.best_label);
		bestLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		best.setText("" + bestScore);
		puzzleButtons[0] = (Button) findViewById(R.id.puzz_button_1);
		puzzleButtons[1] = (Button) findViewById(R.id.puzz_button_2);
		puzzleButtons[2] = (Button) findViewById(R.id.puzz_button_3);
		puzzleButtons[3] = (Button) findViewById(R.id.puzz_button_4);
		operButtons[0] = (Button) findViewById(R.id.oper_button_1);
		operButtons[1] = (Button) findViewById(R.id.oper_button_2);
		operButtons[2] = (Button) findViewById(R.id.oper_button_3);
		operButtons[3] = (Button) findViewById(R.id.oper_button_4);
		operButtons[4] = (Button) findViewById(R.id.oper_button_5);
		operButtons[5] = (Button) findViewById(R.id.oper_button_6);
		submitButton = (Button) findViewById(R.id.submit_button);
		backButton = (Button) findViewById(R.id.back_button);
		clearButton = (Button) findViewById(R.id.clear_button);
		giveUpButton = (Button) findViewById(R.id.end_button);
		for (int i = 0; i < NUMBERS; i++)
		{
			puzzleButtons[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
		}
		for (int i = 0; i < operators.length; i++)
		{
			operators[i] = new Operator(i);
			operButtons[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
		}
		message.setText("Welcome...");
		newPuzzle();
	}
	public void newPuzzle()
	{
		puzzle = new Puzzle(puzzleStrings.get(Functions.getRnd(puzzleStrings.size())));
		score.setText("" + points);
		guess.setText("");
		digitNeeded = true;
		elementList = new ArrayList<>();
		puzzle.resetButtons();
	}
	private void checkHighScore()
	{
		if (points > bestScore)
		{
			bestScore = points;
			best.setText("" + points);
			SharedPreferences.Editor scoreEdit = gamePrefs.edit();
			scoreEdit.putString(BEST, "" + bestScore);
			scoreEdit.commit();
		}
	}
	public void processGiveUp(View view)
	{
		if (gameOver)
		{
			this.finish();
		}
		else
		{	
			guess.setText(puzzleSolution);
			checkHighScore();
			gameOver = true;
			message.setText("Thanks for playing!");
			submitButton.setText("Again?");
			giveUpButton.setText("Quit");
		}	
	}
	public void processOperClick(View view, int operIndex)
	{
		if (operators[operIndex].isParenthesis())				// parentheses
		{
			elementList.add(operators[operIndex]);
			guess.setText(guess.getText()+ operators[operIndex].toString() + " ");
			message.setText("");
		}
		else if (digitNeeded)
		{
			message.setText("Click on a number.");
		}
		else
		{	
			elementList.add(operators[operIndex]);
			guess.setText(guess.getText()+ operators[operIndex].toString() + " ");
			message.setText("");
			digitNeeded = true;
		}	
	}
	public void processPuzzClick(View view, int argIndex)
	{
		if (digitNeeded)
		{	
			message.setText("");
			elementList.add(puzzle.getArgument(argIndex));
			guess.setText(guess.getText().toString() + puzzle.getArgument(argIndex).getNum() + " ");
			puzzle.getArgument(argIndex).getButton().setEnabled(false);
			digitNeeded = false;
		}
		else
		{
			message.setText("Click on an operation sign.");
		}
	}
	public void processSubmit(View view)
	{
		if (gameOver)
		{
			gameOver = false;
			points = 0;
			message.setText("");
			submitButton.setText("Evaluate");
			giveUpButton.setText("Give Up");
			newPuzzle();
		}
		else
		{	
			double value = puzzle.evaluateGuess(guess.getText().toString());
			if (value == TARGET)
			{
	            message.setText("You got it! Here's another!");
	            points += 1;
	            newPuzzle();
			}
			else if (message.getText().toString().equalsIgnoreCase("OK"))
			{
	            String val = "" + Functions.formatDecimals(value, 3);
	            message.setText("No, that equals " + val + ". Try again!");
			}
		}	
	}	
	public void processBackSpace(View view)
	{
//		log("top of processBackSpace, guess is " + guess.getText() + ", digitneeded is " + digitNeeded);
		if (!(elementList.isEmpty()))
		{
			if (elementList.get(elementList.size() - 1).getClass().getName().equals("org.tomar.tomar24.MainActivity$Argument"))
			{
				((Argument) elementList.get(elementList.size() - 1)).getButton().setEnabled(true);
				digitNeeded = true;
			}
			else 			// it's an operator -- if it's parenthesis, leave validation what it was
			{
				if (!((Operator) elementList.get(elementList.size() - 1)).isParenthesis())
				{	
					digitNeeded = false;
				}	
			}
			elementList.remove(elementList.size() - 1);
			StringBuilder sb = new StringBuilder("");
			try
			{
				for (int i = 0; i < elementList.size(); i++)
				{
					sb.append(elementList.get(i) + " ");
				}
			}
			catch (Exception e)
			{
				log("ERROR in ToMar24 backSpace: " + e);
			}
			guess.setText(sb.toString());
		}
//		log("bottom of processBackSpace, guess is " + guess.getText() + ", digitneeded is " + digitNeeded);
	}	
	public void processClear(View view)
	{
		while (elementList.size() > 0)
		{
//			log("calling backspace from clear");
			processBackSpace(view);
		}
	}
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_how)
		{
			openHow();
			return true;
		}
		if (id == R.id.action_about)
		{
			openAbout();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	public void openHow()
	{
	    Intent i = new Intent(MainActivity.this, HowToPlay.class);
	    startActivity(i);
	}	
	public void openAbout()
	{
	    Intent i = new Intent(MainActivity.this, AboutToMarGames.class);
	    startActivity(i);
	}	
	public void processPuzzClick1(View view) 
	{
		processPuzzClick(view, 0);
	}	
	public void processPuzzClick2(View view) 
	{
		processPuzzClick(view, 1);
	}	
	public void processPuzzClick3(View view) 
	{
		processPuzzClick(view, 2);
	}	
	public void processPuzzClick4(View view) 
	{
		processPuzzClick(view, 3);
	}	
	public void processOperClick1(View view) 
	{
		processOperClick(view, 0);
	}	 
	public void processOperClick2(View view) 
	{
		processOperClick(view, 1);
	}	 
	public void processOperClick3(View view) 
	{
		processOperClick(view, 2);
	}	 
	public void processOperClick4(View view) 
	{
		processOperClick(view, 3);
	}	 
	public void processOperClick5(View view) 
	{
		processOperClick(view, 4);
	}	 
	public void processOperClick6(View view) 
	{
		processOperClick(view, 5);
	}
	
    private class Puzzle
    {
        private Argument[] argument;
    	private tmEvaluator eval = new tmEvaluator();
        public static final int NUMBERS = 4;
        public static final String SPACE = " ";

        public Puzzle(String puzzleString)
        {
			String[] holder = new String[NUMBERS];
			int[] picker = Functions.randomPicks(NUMBERS, NUMBERS);
			holder[picker[0]] = puzzleString.substring(0, puzzleString.indexOf(SPACE));
			puzzleString = puzzleString.substring(puzzleString.indexOf(SPACE)+ 1);
			holder[picker[1]] = puzzleString.substring(0, puzzleString.indexOf(SPACE));
			puzzleString = puzzleString.substring(puzzleString.indexOf(SPACE)+ 1);
			holder[picker[2]] = puzzleString.substring(0, puzzleString.indexOf(SPACE));
			puzzleString = puzzleString.substring(puzzleString.indexOf(SPACE)+ 1);
			holder[picker[3]] = puzzleString.substring(0, puzzleString.indexOf(SPACE));
			puzzleSolution = puzzleString.substring(puzzleString.indexOf(SPACE)+ 1);
            argument = new Argument[NUMBERS];
			StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < NUMBERS; i++)
            {
                argument[i] = new Argument(holder[i].trim(), i);
				sb.append(Functions.formatNumber(argument[i].getNum(), 2));
            }
        }
        public String solvePuzzle()				// no longer being called, but useful to preserve
        {
        	for (int i1 = 0; i1 < NUMBERS; i1++)
        	{
        		for (int i2 = 0; i2 < NUMBERS; i2++)
        		{
        			if (i2 == i1)
        			{
        				continue;
        			}
        			for (int i3 = 0; i3 < NUMBERS; i3++)
        			{
        				if (i3 == i2 || i3 == i1)
        				{
        					continue;
        				}
        				for (int i4 = 0; i4 < NUMBERS; i4++)
        				{
        					if (i4 == i3 || i4 == i2 || i4 == i1)
        					{
        						continue;
        					}
        					for (int o1 = 0; o1 < NUMBERS; o1++)
        					{
        						for (int o2 = 0; o2 < NUMBERS; o2++)
        						{
        							for (int o3 = 0; o3 < NUMBERS; o3++)
        							{
        								String answer = checkTemplates(this.getArgument(i1).getNum(),this.getArgument(i2).getNum(),this.getArgument(i3).getNum(),this.getArgument(i4).getNum(),o1,o2,o3);
        								if (answer != null)
										{	// only going to get one solution, but you could collect them here
        									return answer;
										}
        							}
        						}
        					}
        				}
        			}
        		}
        	}
        	return null;
        }
    	public String checkTemplates(int i1, int i2, int i3, int i4, int o1, int o2, int o3)
    	{
    		String expr = "";
    		// no parens
    		expr = "" + i1 + operators[o1].toString() + i2 + operators[o2].toString() + i3 + operators[o3].toString() + i4;
    		if (eval.evaluate(expr) == TARGET) 
    		{
    			return expr;
    		}
    		// (a,b) c d
    		expr = "(" + i1 + operators[o1].toString() + i2 + ")" + operators[o2].toString() + i3 + operators[o3].toString() + i4;
    		if (eval.evaluate(expr) == TARGET)
    		{
    			return expr;
    		}
    		// a (b,c) d
    		expr = "" + i1 + operators[o1].toString() + "(" + i2 + operators[o2].toString() + i3 + ")" + operators[o3].toString() + i4;
    		if (eval.evaluate(expr) == TARGET)
    		{
    			return expr;
    		}
    		// a b (c,d)
    		expr = "" + i1 + operators[o1].toString() + i2 + operators[o2].toString() + "(" + i3 + operators[o3].toString() + i4 + ")";
    		if (eval.evaluate(expr) == TARGET)
    		{
    			return expr;
    		}
    		// (a,b)(c,d)
    		expr = "(" + i1 + operators[o1].toString() + i2 + ")" + operators[o2].toString() + "(" + i3 + operators[o3].toString() + i4 + ")";
    		if (eval.evaluate(expr) == TARGET)
    		{
    			return expr;
    		}
    		// (a,b,c) d
    		expr = "(" + i1 + operators[o1].toString() + i2 + operators[o2].toString() + i3 + ")" + operators[o3].toString() + i4; 
    		if (eval.evaluate(expr) == TARGET)
    		{
    			return expr;
    		}
    		// a (b,c,d)
    		expr = "" + i1 + operators[o1].toString() + "(" + i2 + operators[o2].toString() + i3 + operators[o3].toString() + i4 + ")";
    		if (eval.evaluate(expr) == TARGET)
    		{
    			return expr;
    		}
    		return null;
    	}
		public void resetButtons()
        {
            for (int i = 0; i < NUMBERS; i++)
            {
                argument[i].resetB();
            }
        }
        public Argument getArgument(int i)
        {
            return argument[i];
        }
        public double evaluateGuess(String expr)
        {
        	for (int i = 0; i < Puzzle.NUMBERS; i++)
            {
                if (puzzle.getArgument(i).getButton().isEnabled())
                {
                    message.setText("Failed to use the number " + puzzle.getArgument(i).getNum() + ".");
                    return tmEvaluator.ERRORFLAG;
                }
            }
            double value = eval.evaluate(expr);
            message.setText(eval.getMessage());
            return value;
        }
    }
    private class Argument
    {
    	private Button button = null;
		private int num;
        
        public Argument(String s, int index)
        {
            this.num = Integer.parseInt(s);
            this.button = puzzleButtons[index];
        }
        public void resetB()
        {
        	button.setText("" + num);
        	button.setEnabled(true);
        }
        public Button getButton()
		{
			return this.button;
		}
    	public int getNum()
        {
            return this.num;
        }
        public String toString()
        {
        	return "" + this.num;
        }
    }
    private class Operator
    {
    	private Button button = null;
		private boolean parenthesis = false;
        
        public Operator(int index)
        {
            this.button = operButtons[index];
            if (index > 3)
            {
            	parenthesis = true;
            }
        }
        public Button getButton()
		{
			return this.button;
		}
    	public boolean isParenthesis()
        {
            return this.parenthesis;
        }
        public String toString()
        {
        	return "" + this.getButton().getText();
        }
    }
    
}
