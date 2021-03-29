clean:
	rm -rf classes/*

compile-game-files:
	javac -d "classes" -sourcepath 2048/ 2048/src/game/Game.java
	
compile-solver: compile-game-files
	javac -d "classes" -classpath "classes" -sourcepath 2048/ 2048/src/solver/Main.java

cross-validation: clean compile-solver
	mkdir -p dumps
	touch dumps/validation.txt
	for vC in 0.02 0.035 0.07 ; do \
		for pC in 0.6 0.8 1.0 ; do \
			for gsC in 0.3 0.5 0.7 ; do \
				echo $$vC $$pC $$gsC ; \
				java -classpath "classes" src/solver/Main \
					-vari $$vC \
					-place $$pC \
					-groupspread $$gsC \
					-empty 30 \
					-merge 7.5 \
					>> dumps/validation.txt ; \
   			done \
		done \
	done

check-consistency: clean compile-solver
	mkdir -p dumps
	touch dumps/consistency.txt
	for val in 1 2 3 4 5 6 7 8 9 10 ; do \
		echo $$val ; \
		java -classpath "classes" src/solver/Main \
			-vari 0.02 \
			-place 1.0 \
			-groupspread 0.5 \
			-empty 30 \
			-merge 7.5 \
			>> dumps/consistency.txt ; \
	done

run: clean compile-solver
	java -classpath "classes" src/solver/Main