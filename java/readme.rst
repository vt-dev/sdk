========
Examples
========

.. contents:: Table of Contents

Run fuzzing example
-------------------

.. code:: bash

    mvn install
    mvn compile exec:java -pl examples -Dexec.mainClass="Fuzzing" -Dexec.args="-k <key> -s <secret>"
