## Project Introduction

This project is a Java platform for solving the Collaborative Edge Data Caching (CEDC) Problem, with the following main modules:

*   **Problem Generator**:&#x20;
    *   The module generates CEDC problem instances in XML format based on nine configurable parameters：
        *   Number of Instances: The number of problem instances generated at once.
        *   Number of Servers: The number of edge servers in the problem.
        *   Number of Users: The total number of users.
        *   Number of Timeslots: The number of time slots describing the scenario.
        *   Density: The density of the edge network, defined as the ratio of the number of connections between edge servers to the number of edge servers.
        *   RequireRate: The probability of each user requesting data at each time slot.
        *   DataNum: The total number of popular data items in the problem.
        *   ServerDataLimit: The cache space pre-allocated on each edge server by the service provider.
        *   LatencyLimit:  Latency limit.
*   **Problem Parser**: The module is responsible for parsing the problem structure based on the XML file of the problem.
*   **Problem Slover**: The module is responsible for solving the CEDC problem by applying algorithms to generate caching strategies based on the problem parameters.

## Catalog Structure Description

    project-root/
    ├── src/                                  # Source code directory
    │   ├── main/                             # Main application source
    │   └── java/                             # Java source code
    │       ├── algorithm/                    # DSA-CEDC and MGM-CEDC algorithms
    │       ├── problem_generator/            # Problem Generation
    │       ├── problem_parser/               # Problem Parser
    │       └── Test/                         # Test code
    ├── readme                                # Help document
    ├── problem/                              # CEDC problem data
    └── pom.xml                                # Maven configuration file

## Usage Instructions&#x20;

In the directory `src/main/java/Test/dynamic/`, locate the execution file corresponding to the distributed algorithm designed to solve the problem. For example, for DSA-CEDC, the corresponding file is `DynamicMainDSA_CEDC1.java`.

You can modify the following code to solve different instances of the CEDC problem:

    String problemDir = "problem\\edc_dcop\\30200\\dataNum4";   // This refers to solving the problem at problem\\edc_dcop\\30200\\dataNum4
    String outDir = "problem\\ans\\myans\\30200";               // This refers to the storage location for the results after solving

## Dependency Configuration Example

If you are using Maven, please add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.24</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.jdom</groupId>
        <artifactId>jdom2</artifactId>
        <version>2.0.6.1</version>
    </dependency>
</dependencies>

```

