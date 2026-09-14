# IMAS-Java

This repository contains the Java data access library for IMAS. 
It allows to manipulate Java data structures that correspond to IDS defined in the 
[IMAS-Data-Dictionary](https://github.com/iterorganization/IMAS-Data-Dictionary).
It relies on the [IMAS-Core](https://github.com/iterorganization/IMAS-Core) library 
to abstract I/O operations from the underlying chosen data storage format. 


## Getting started

The latest build, install and user documentation is available [here](https://imas-java.readthedocs.io/en/latest/). 

To route the JNI binding through the multiversion DD shim, install
[IMAS-Multiversion-DD-Loader](https://github.com/yohannmarguier/IMAS-Multiversion-DD-Loader)
and configure with `-D AL_USE_MULTIVERSION_SHIM=ON` and
`-D CMAKE_PREFIX_PATH=/path/to/shim/install`. IMAS-Core is still needed for
headers and at runtime. CTest supplies its build-tree Core path and the HLI's
DD version automatically; for a manual Java run, set `IMAS_CORE_LIBRARY` to
the Core shared library and `IMAS_MVDD_HLI_DD_VERSION` to the DD version used
to generate this Java build. See [the shim integration contract](docs/SHIM_INTEGRATION_CONTRACT.md)
for supported conversion scenarios.


## Legal

IMAS-Java is licensed under [LGPL 3.0](LICENSE.txt). 


## Acknowledgements

Bootstrapped from the UAL's javainterface.
