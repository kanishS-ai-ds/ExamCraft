package Kanish.example.ExamCraft.config;

import Kanish.example.ExamCraft.entity.Question;
import Kanish.example.ExamCraft.entity.Unit;
import Kanish.example.ExamCraft.repository.QuestionRepository;
import Kanish.example.ExamCraft.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UnitRepository unitRepository;
    private final QuestionRepository questionRepository;

    public DataInitializer(UnitRepository unitRepository, QuestionRepository questionRepository) {
        this.unitRepository = unitRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        if (unitRepository.count() > 0) {
            log.info("Database already seeded with units and questions. Skipping initialization.");
            return;
        }

        log.info("Seeding initial DBMS Units and Question Bank for ExamCraft...");

        Unit u1 = unitRepository.save(new Unit("Unit 1: Introduction to DBMS & Architecture", "Covers DBMS concept, three-schema architecture, data independence, and data models."));
        Unit u2 = unitRepository.save(new Unit("Unit 2: Relational Model & SQL Constraints", "Covers relational schema, keys, relational algebra, SQL DDL/DML, and integrity constraints."));
        Unit u3 = unitRepository.save(new Unit("Unit 3: Database Normalization & ER Modeling", "Covers functional dependencies, 1NF, 2NF, 3NF, BCNF, and lossless decomposition."));
        Unit u4 = unitRepository.save(new Unit("Unit 4: Transaction Management & ACID Properties", "Covers transaction states, ACID properties, serializability, and conflict graphs."));
        Unit u5 = unitRepository.save(new Unit("Unit 5: Concurrency Control & Recovery", "Covers 2-Phase Locking (2PL), deadlock handling, write-ahead logging, and B+ Trees."));

        List<Question> questions = Arrays.asList(
                // Unit 1 Questions
                new Question(
                        "Which level of the ANSI-SPARC three-tier architecture describes the physical storage structure of the database?",
                        "Internal / Physical Level",
                        "Conceptual Level",
                        "External / View Level",
                        "Logical Level",
                        "A",
                        "EASY",
                        "DBMS Architecture",
                        u1
                ),
                new Question(
                        "What is the primary advantage of Logical Data Independence in a database management system?",
                        "Allows altering conceptual schema without altering external views/programs",
                        "Allows physical storage changes without altering conceptual schema",
                        "Eliminates all data redundancy completely",
                        "Prevents SQL injection attacks automatically",
                        "A",
                        "MEDIUM",
                        "Data Independence",
                        u1
                ),
                new Question(
                        "Which data model represents data as collections of trees and records linked in parent-child relationships?",
                        "Hierarchical Model",
                        "Network Model",
                        "Relational Model",
                        "Object-Oriented Model",
                        "A",
                        "EASY",
                        "Data Models",
                        u1
                ),
                new Question(
                        "In the ANSI/SPARC 3-tier architecture, what mapping translates between user views and the conceptual schema?",
                        "External-Conceptual Mapping",
                        "Conceptual-Internal Mapping",
                        "Physical-Storage Mapping",
                        "Network-Host Mapping",
                        "A",
                        "HARD",
                        "DBMS Architecture",
                        u1
                ),

                // Unit 2 Questions
                new Question(
                        "Which relational algebra operator selects tuples that satisfy a given predicate?",
                        "Selection (σ)",
                        "Projection (π)",
                        "Cartesian Product (×)",
                        "Union (∪)",
                        "A",
                        "EASY",
                        "Relational Algebra",
                        u2
                ),
                new Question(
                        "A primary key constraint in SQL enforces which of the following integrity rules?",
                        "Entity Integrity (Unique and NOT NULL)",
                        "Referential Integrity only",
                        "Domain Integrity only",
                        "User-Defined Integrity only",
                        "A",
                        "EASY",
                        "SQL Constraints",
                        u2
                ),
                new Question(
                        "What is the result of applying a NATURAL JOIN between two tables with no common attributes?",
                        "Cartesian Product (Cross Join)",
                        "Empty relation (0 tuples)",
                        "Syntax Error",
                        "Inner Join with NULLs",
                        "A",
                        "MEDIUM",
                        "SQL Joins",
                        u2
                ),
                new Question(
                        "In relational algebra, which operator is considered NON-derivative (fundamental) along with Selection, Projection, Union, and Cartesian Product?",
                        "Set Difference (−)",
                        "Intersection (∩)",
                        "Natural Join (⋈)",
                        "Division (÷)",
                        "A",
                        "HARD",
                        "Relational Algebra",
                        u2
                ),
                new Question(
                        "Which clause in SQL is used to filter groups created by the GROUP BY clause?",
                        "HAVING",
                        "WHERE",
                        "ORDER BY",
                        "FILTER",
                        "A",
                        "EASY",
                        "SQL Aggregation",
                        u2
                ),

                // Unit 3 Questions
                new Question(
                        "A relation is in 1NF if and only if all attribute values are:",
                        "Atomic (indivisible values)",
                        "Primary keys",
                        "Foreign keys",
                        "Free of transitive dependencies",
                        "A",
                        "EASY",
                        "Normalization",
                        u3
                ),
                new Question(
                        "Which normal form eliminates partial functional dependencies on candidate keys?",
                        "Second Normal Form (2NF)",
                        "First Normal Form (1NF)",
                        "Third Normal Form (3NF)",
                        "Boyce-Codd Normal Form (BCNF)",
                        "A",
                        "MEDIUM",
                        "Normalization",
                        u3
                ),
                new Question(
                        "If relation R(A, B, C) has functional dependency A -> B and B -> C, the dependency A -> C is an example of:",
                        "Transitive Dependency",
                        "Partial Dependency",
                        "Trivial Dependency",
                        "Multivalued Dependency",
                        "A",
                        "MEDIUM",
                        "Functional Dependencies",
                        u3
                ),
                new Question(
                        "A relation R is in BCNF if for every non-trivial functional dependency X -> Y, X must be a:",
                        "Superkey",
                        "Prime Attribute",
                        "Foreign Key",
                        "Composite Key",
                        "A",
                        "HARD",
                        "BCNF",
                        u3
                ),
                new Question(
                        "Under what condition is a decomposition of relation R into R1 and R2 guaranteed to be lossless join?",
                        "(R1 ∩ R2) -> R1 or (R1 ∩ R2) -> R2",
                        "(R1 ∪ R2) = R",
                        "R1 and R2 have disjoint attributes",
                        "Neither R1 nor R2 has NULL values",
                        "A",
                        "HARD",
                        "Lossless Decomposition",
                        u3
                ),

                // Unit 4 Questions
                new Question(
                        "Which ACID property guarantees that all operations within a transaction succeed together or all fail together?",
                        "Atomicity",
                        "Consistency",
                        "Isolation",
                        "Durability",
                        "A",
                        "EASY",
                        "ACID Properties",
                        u4
                ),
                new Question(
                        "Which transaction state occurs when a transaction has completed its final operation, but changes are not yet flushed to disk?",
                        "Partially Committed",
                        "Committed",
                        "Active",
                        "Failed",
                        "A",
                        "MEDIUM",
                        "Transaction States",
                        u4
                ),
                new Question(
                        "A schedule is conflict serializable if and only if its precedence (serialization) graph contains:",
                        "No cycles (Acyclic)",
                        "At least one directed cycle",
                        "Only self-loops",
                        "A complete bipartite subgraph",
                        "A",
                        "HARD",
                        "Conflict Serializability",
                        u4
                ),
                new Question(
                        "Which problem occurs when one transaction reads an uncommitted value modified by another concurrent transaction that later rolls back?",
                        "Dirty Read (Uncommitted Dependency)",
                        "Lost Update",
                        "Non-repeatable Read",
                        "Phantom Read",
                        "A",
                        "MEDIUM",
                        "Isolation Anomalies",
                        u4
                ),

                // Unit 5 Questions
                new Question(
                        "In Strict Two-Phase Locking (Strict 2PL), when are exclusive locks released?",
                        "Only after the transaction commits or aborts",
                        "Immediately after the write operation finishes",
                        "During the shrinking phase before commit",
                        "When another transaction requests a shared lock",
                        "A",
                        "MEDIUM",
                        "Concurrency Control",
                        u5
                ),
                new Question(
                        "What is a major advantage of B+ Tree indexing over standard B Tree indexing in relational databases?",
                        "All data pointers reside only in leaf nodes, allowing fast sequential scans via linked leaves",
                        "B+ Trees consume zero disk space",
                        "B+ Trees eliminate locking completely",
                        "B+ Trees do not require rebalancing on insertions",
                        "A",
                        "HARD",
                        "Indexing",
                        u5
                ),
                new Question(
                        "Which technique does the DBMS Write-Ahead Logging (WAL) protocol mandate before committing changes to disk?",
                        "Log records must be written to stable storage before the corresponding data page is written",
                        "Data pages must be flushed before writing log records",
                        "Both log and data must be written simultaneously in memory only",
                        "Logs are only generated during rollback",
                        "A",
                        "HARD",
                        "Database Recovery",
                        u5
                ),
                new Question(
                        "Which graph is maintained by the database engine to detect deadlocks among concurrent transactions?",
                        "Wait-For Graph (WFG)",
                        "Precedence Graph",
                        "Conflict Graph",
                        "Call Graph",
                        "A",
                        "EASY",
                        "Deadlock Handling",
                        u5
                )
        );

        questionRepository.saveAll(questions);
        log.info("Successfully seeded {} DBMS questions across 5 units!", questions.size());
    }
}
