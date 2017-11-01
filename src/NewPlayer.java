import java.util.Arrays;

import de.ovgu.dke.teaching.ml.tictactoe.api.IBoard;
import de.ovgu.dke.teaching.ml.tictactoe.api.IMove;
import de.ovgu.dke.teaching.ml.tictactoe.api.IPlayer;
import de.ovgu.dke.teaching.ml.tictactoe.api.IllegalMoveException;
import de.ovgu.dke.teaching.ml.tictactoe.game.Move;
import de.ovgu.dke.teaching.ml.tictactoe.util.Indexing;

/**
 * 
 * @author Marten Wallewein-Eising, Johann Wagner, Johannes Wuensche, Paul Stang
 */

public class NewPlayer implements IPlayer {
	//float w0, w1, w2, w3, w4;
	float learningRate = 0.001f;
	/**
	 * Backup of implementation in https://github.com/jwuensche/TicToeTac/commit/a9d015965f726a3761f8bcf3622fa80b2153da36
	 */
	float[] weights2 = {1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 }; // for the 4x,3x,2x,1x and 4o,3o,2o,1o
	float[] weights = {0,10,-10,20,-20};//{0, 10, -10, -10, 10 };//{0.0f, 20.906f, -0.8389592f, -7.8188f, 12.1812f};//{0.0f, 13.5f, -7.0599995f, -9.3f, 10.7f};//{0, 10, -10, -10, 10 };
	
	public NewPlayer(){
		
	}
	
	public String getName() {
		// TODO Auto-generated method stub
		return "TicToeTac";
	}


	public int[] makeMove(IBoard board)
	{	
		// create a clone of the board that can be modified
		int[] currentParams = new int[board.getDimensions()];
		IMove bestMove = null;
		float bestScore = Integer.MIN_VALUE, currentScore = 0;
		
		do
		{
			IBoard copy = board.clone();
			if(board.getFieldValue(currentParams) != null)
				continue;
			
			// do a move using the cloned board
			try {
				
				Move currentMove = new Move(this, currentParams.clone());
				copy.makeMove(currentMove);
				
				currentScore = classifyBoard(copy);
				if(currentScore > bestScore)
				{
					bestMove = currentMove;
					bestScore = currentScore;
				}
				
			} catch (IllegalMoveException e) {
				// move was not allowed
			}
			
		}
		while(Indexing.incrementIndices(currentParams, board.getSize()));
		
		return bestMove.getPosition();
	}
	
	/**
	 * Backup of implementation in https://github.com/jwuensche/TicToeTac/commit/a9d015965f726a3761f8bcf3622fa80b2153da36
	 * @param board
	 * @return
	 */
	public int[] makeMove2(IBoard board) {
		// TODO Auto-generated method stub
		int [] makeMove = new int[3];
		double boardScore = Double.MIN_VALUE;
		// create a clone of the board that can be modified
		IBoard copy = board.clone();
		IBoard tmp = board.clone();
		//create an additional board and if the next move == winner then make this move
		// if not go through every possible move and choose the one with the highest score
		for(int x = 0;x<5;x++)
		{
			for(int y = 0;y<5;y++)
			{
				for(int z = 0;z<5;z++)
				{
					makeMove= new int[]{x,y,z};
					try 
					{
						tmp.makeMove(new Move(this, makeMove));
						if(tmp.isFinalState())
							if(tmp.getWinner()==this) 
								return makeMove;
						
					}
					catch (IllegalMoveException e) 
					{
						// move was not allowed
					}
					if(copy.getFieldValue(makeMove)==null)
					{
						try 
						{
							int []testMove = new int []{x,y,z};
							copy.makeMove(new Move(this, makeMove));
							int[] testarray = new int[8] ; //something that evaluates the board on the number of 4x,3x,2x,1x and 4o,3o,2o,1o
							double scoreTest = 	weights[0] +weights[1]*testarray[0] +weights[2]*testarray[1] +weights[3]*testarray[2] 
												+weights[4]*testarray[3] +weights[5]*testarray[4] +weights[6]*testarray[5] 
												+weights[7]*testarray[6] +weights[8]*testarray[7]; 
							//summ of weights multiplied by the testarray
							if(boardScore>scoreTest)
								boardScore = scoreTest;
								makeMove=testMove;
							
						} 
						catch (IllegalMoveException e) 
						{
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
					}
					
				}	
			}
		}
		// do a move using the cloned board
		

		// return your final decision for your next move
		return makeMove;
	}
	
	/**
	 *  Least mean square error algorithm learning the weights based on the given error
	 * @param boardPrev Last board state
	 * @param boardAft Current board state
	 */
	private void LMS(IBoard boardPrev, IBoard boardAft)
	{
		float moveValue = this.classifyMove(boardPrev, boardAft);
		LMS(boardAft, moveValue);
	}
	
	/**
	 * Least mean square error algorithm learning the weights based on the given error
	 * @param board Current Board state
	 * @param error Training error, @see documentation
	 */
	private void LMS(IBoard board, float error){
		int[] params = this.getParameters(board);

		for(int i = 0; i < params.length; ++i)
		{
			this.weights[i] = this.weights[i] + this.learningRate * params[i] * error;
		}
	}
	
	/**
	 * Calculates the difference of the board scores of the last and current board
	 * @param boardPrev Last board state
	 * @param boardAft Current board state@param boardPrev
	 * @return Score difference
	 */
	private float classifyMove(IBoard boardPrev, IBoard boardAft){
		return this.classifyBoard(boardAft) - this.classifyBoard(boardPrev);
	}
	
	/**
	 * Calculates the Board score based on the current weights
	 * @param board Current Board state
	 * @return Board score
	 */
	private float classifyBoard(IBoard board){
		
		int [] params = this.getParameters(board);
		float sum = 0;
		for(int i = 0; i < params.length; ++i)
		{
			sum += this.weights[i] * params[i];
		}
		
		return sum;
		//return this.w0 + this.w1 * param[0] + this.w2 * param[1] + this.w3 * param[2] + this.w4 * param[3];
		
	}
	/**
	 * Returns the Score parameters which are used for LMS
	 * Definition of parameters:
	 * x0: 1
	 * x1: Count of own rows, which can be completed
	 * x2: Count of enemy rows, which can be completed
	 * x3: Count of min marks required to fill a row
	 * x4: Count of min enemy marks required to fill a row
	 * @param board
	 * @return int[] of x0, ..., xi
	 */
	private int[] getParameters(IBoard board){
		int[] index1 = {0,0,0};
		int[] index2 = {0,0,0};
		int[] param = {0,0,0,board.getSize(), board.getSize()};
		//Notation for already seen players on a row
		IPlayer[] rows =new IPlayer[board.getSize()]; 
		int layers = 1;
		if(board.getDimensions() > 2)
			layers = board.getSize();
		
		//Begin search structure
		while (layers > 0){
		layers--;
		index1[2] = index2[2] = layers;
		for(int column = 0; column < board.getSize(); column++){
			index1[0] = column;
			//Notation for already seen Player in a column
			IPlayer columns = null;
			int marks = board.getSize();

			for(int row = 0; row < board.getSize(); row++){
				index1[1] = row;
				//Check for occupation
				if(board.getFieldValue(index1) != null && columns == null)
					columns = board.getFieldValue(index1);
				//Check for occupation conflict
				else if(board.getFieldValue(index1) != null && columns != board.getFieldValue(index1))
					break;
				//Check for occupation continuation
				else if(board.getFieldValue(index1) != null && columns == board.getFieldValue(index1))
					marks--;
				//Check for end
				if(row == board.getSize() - 1){
					if(this != columns){
						param[2]++;
						if(marks < param[4])
							param[4] = marks;
					}
					else{
						param[1]++;
						if(marks < param[3])
							param[3] = marks;
					}
				}
			}
			// ------ end column search
		}

		for(int row = 0; row < board.getSize(); row++){
			index2[1] = row;
			//Notation for already seen Player in a column
			IPlayer columns = null;
			int marks = board.getSize();

			for(int column = 0; column < board.getSize(); column++){
				index2[0] = column;
				//Check for occupation
				if(board.getFieldValue(index2) != null && columns == null)
					columns = board.getFieldValue(index2);
				//Check for occupation conflict
				else if(board.getFieldValue(index2) != null && columns != board.getFieldValue(index2))
					break;
				//Check for occupation continuation
				else if(board.getFieldValue(index2) != null && columns == board.getFieldValue(index2))
					marks--;
				//Check for end
				if(row == board.getSize() - 1){
					if(this != columns){
						param[2]++;
						if(marks < param[4])
							param[4] = marks;
					}
					else{
						param[1]++;
						if(marks < param[3])
							param[3] = marks;
					}
				}
			}
			// ------ end row search
		}
	}
		return param;
	}

	/**
	 * Learns the weights based on the winner of the match
	 */
	public void onMatchEnds(IBoard board) {
		// calculate error
		float currentBoardState = 0;
		if(board.getWinner() == null)
			currentBoardState = 50;
		else if(board.getWinner() == this)
			currentBoardState = 100;
		else
			currentBoardState = -100;
		
		float error = Math.abs(classifyBoard(board) - currentBoardState);
		LMS(board, error);
		
		return;
	}

}
