========
Examples
========

.. contents:: Table of Contents

Run fuzzing example
-------------------

.. code:: bash

    mvn install
    mvn compile exec:java -pl examples -Dexec.mainClass="Fuzzing" -Dexec.args="-h vt.dev -p 80 -k <key> -s <secret> -t"

