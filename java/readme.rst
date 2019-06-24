========
Examples
========

.. contents:: Table of Contents

Run fuzzing example
-------------------

.. code:: bash

    mvn install
    mvn compile exec:java -pl examples -Dexec.mainClass="Fuzzing" -Dexec.args="-k <key> -s <secret>"

With demo key/secret:

.. code:: bash

    mvn compile exec:java -pl examples -Dexec.mainClass="Fuzzing" -Dexec.args="-k 0dee4f2e-4b05-445d-b8ea-f8fe6b4b772c -s 1bcdd94c-a7ed-4e24-b601-6e8753ca3721"
