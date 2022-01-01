build:
	javac -d . ./ChessEngine/src/MyScanner.java
	javac -d . ./ChessEngine/src/Pieces.java
	javac -d . ./ChessEngine/src/Position.java
	javac -d . ./ChessEngine/src/Move.java
	javac -d . ./ChessEngine/src/PromotionMove.java
	javac -d . ./ChessEngine/src/QueenSideCastleMove.java
	javac -d . ./ChessEngine/src/KingSideCastleMove.java
	javac -d . ./ChessEngine/src/CharMatrix.java
	javac -d . ./ChessEngine/src/State.java
	javac -d . ./ChessEngine/src/Player.java
	javac -d . ./ChessEngine/src/Game.java ./ChessEngine/src/MinimaxState.java
	javac -d . ./ChessEngine/src/XBoard.java
	jar cvmf MANIFEST.MF bot.jar *.class
clean: 
	rm -f *.class
	rm -f bot.jar
	rm -f xboard.debug
	rm -f debug.txt
run:
	java -jar bot.jar
