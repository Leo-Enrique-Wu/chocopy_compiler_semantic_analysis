# NYU Compiler Construction CSCI-GA.2130/Spring 2021: Programming Assignment 2

This assignment is adapted from https://github.com/cs164berkeley/pa2-chocopy-semantic-analysis with the authors' permission.

See the PA2 document on Piazza for a detailed specification.

## Quickstart

Run the following commands to compile your analyzer and run the tests:
```
mvn clean package
java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy \
  --pass=.s --test --dir src/test/data/pa2/sample/
```

The dot in `--pass` makes the compiler skip parsing and go straight to semantic analysis.
`--pass=.s` uses your (`s` for `student`) analyzer to perform semantic analysis on a preparsed input (in this case, the `.ast` files under `src/test/data/pa2/sample/`).
With the starter code, only two tests should pass.
Your main objective is to build an analyzer that passes all the provided tests.

`--pass=.r` uses the reference (`r` for `reference`) analyzer, which should pass all tests.

In addition to running in test mode with `--test`, you can also observe the actual output of your (or reference) analyzer with:
```
java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy \
  --pass=.s src/test/data/pa2/sample/expr_unary.py.ast
```

You can also run both passes on the original `.py` file:
```
java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy \
  --pass=rr src/test/data/pa2/sample/expr_unary.py
```

Once you merge your parser code from assignment 1, you should be able to use `--pass=ss`.
