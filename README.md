# 02180_first_assignment
This project is an algorithm for the game 2048. \
It is using expectimax as the foundation for the solver. \
It has been created using Java 11 and 15. \
The module [game](2048/src/game) contains the game implementation which is NOT created as a part of this project. The AI-solver is found [here](2048/src/solver).
The folder [dumps](dumps) contains the raw output from cross-validation and consistency test. 

### Running through IDE
1. Set the folder 2048 as source
2. Run main without any args

### Running through makefile (this will handle project compiling)
1. In the repository root run the command `make run`