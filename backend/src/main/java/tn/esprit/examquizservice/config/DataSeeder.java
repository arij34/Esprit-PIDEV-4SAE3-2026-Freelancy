package tn.esprit.examquizservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.examquizservice.entities.*;
import tn.esprit.examquizservice.repositories.ExamRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds 10 production-ready exams (20 MCQ questions × 5 pts = 100 pts each).
 * Only runs when the exams table is empty — fully idempotent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final ExamRepository examRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (examRepository.count() > 0) {
            log.info("DataSeeder: exams table already populated, skipping seed.");
            return;
        }
        log.info("DataSeeder: seeding 10 exams...");
        buildExams().forEach(examRepository::save);
        log.info("DataSeeder: done.");
    }

    // -------------------------------------------------------------------------
    // Exam builders
    // -------------------------------------------------------------------------

    private List<Exam> buildExams() {
        return List.of(
            buildExam("Java Fundamentals",
                "Core Java concepts: OOP, collections, exceptions, generics, and more.",
                javaQuestions()),
            buildExam("Spring Boot Essentials",
                "Spring Boot auto-configuration, REST controllers, JPA, and security basics.",
                springQuestions()),
            buildExam("JavaScript Essentials",
                "ES6+ features, async/await, DOM manipulation, and module system.",
                jsQuestions()),
            buildExam("SQL & Relational Databases",
                "DDL, DML, joins, indexes, transactions, and normalization.",
                sqlQuestions()),
            buildExam("REST API Design",
                "HTTP verbs, status codes, versioning, authentication, and best practices.",
                restQuestions()),
            buildExam("Python Basics",
                "Python syntax, data types, comprehensions, decorators, and file I/O.",
                pythonQuestions()),
            buildExam("Git & Version Control",
                "Branching, merging, rebasing, tagging, and collaborative workflows.",
                gitQuestions()),
            buildExam("HTML & CSS Fundamentals",
                "Semantic HTML5, CSS box model, flexbox, grid, and responsive design.",
                htmlCssQuestions()),
            buildExam("Agile & Scrum",
                "Scrum roles, ceremonies, artifacts, sprint planning, and Kanban basics.",
                agileQuestions()),
            buildExam("Cybersecurity Basics",
                "OWASP Top 10, encryption, authentication, XSS, SQL injection, and secure coding.",
                cyberQuestions())
        );
    }

    private Exam buildExam(String title, String description, List<Question> questions) {
        Exam exam = Exam.builder()
                .title(title)
                .description(description)
                .duration(45)
                .points(100.0)
                .passingScore(50.0)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(1))
                .maxAttempts(3)
                .createdBy("admin")
                .createdAt(LocalDateTime.now())
                .status(ExamStatus.PUBLISHED)
                .examType(ExamType.EXAM)
                .build();

        questions.forEach(q -> q.setExam(exam));
        exam.setQuestions(questions);
        return exam;
    }

    // -------------------------------------------------------------------------
    // Helper: build one MCQ question with 4 choices, one correct
    // -------------------------------------------------------------------------

    private Question q(int order, String text, String a, String b, String c, String d, int correctIndex) {
        List<Answer> answers = new ArrayList<>();
        String[] opts = {a, b, c, d};
        for (int i = 0; i < 4; i++) {
            Answer ans = Answer.builder()
                    .answerText(opts[i])
                    .isCorrect(i == correctIndex)
                    .orderIndex(i + 1)
                    .build();
            answers.add(ans);
        }
        Question question = Question.builder()
                .questionText(text)
                .questionType("MCQ")
                .points(5.0)
                .difficultyLevel("MEDIUM")
                .orderIndex(order)
                .answers(answers)
                .build();
        answers.forEach(ans -> ans.setQuestion(question));
        return question;
    }

    // -------------------------------------------------------------------------
    // 1 · Java Fundamentals (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> javaQuestions() {
        return List.of(
            q(1,  "Which keyword is used to prevent a class from being subclassed?",
                "static", "final", "abstract", "sealed", 1),
            q(2,  "What is the default value of a boolean field in a Java class?",
                "true", "null", "false", "0", 2),
            q(3,  "Which collection guarantees insertion-order and allows duplicates?",
                "HashSet", "TreeSet", "ArrayList", "LinkedHashSet", 2),
            q(4,  "What does the 'volatile' keyword guarantee?",
                "Atomicity of compound operations", "Visibility of writes across threads",
                "Prevention of deadlocks", "Mutual exclusion", 1),
            q(5,  "Which interface must a lambda implement to be used as a Runnable?",
                "Callable", "Runnable", "Supplier", "Function", 1),
            q(6,  "What is the time complexity of HashMap.get() on average?",
                "O(n)", "O(log n)", "O(1)", "O(n log n)", 2),
            q(7,  "Which exception is thrown when you access an array index out of range?",
                "NullPointerException", "IndexOutOfBoundsException",
                "ArrayIndexOutOfBoundsException", "IllegalArgumentException", 2),
            q(8,  "What does 'static' mean on a nested class?",
                "It can only access static outer members", "It cannot be instantiated",
                "It inherits the outer class", "It is thread-safe", 0),
            q(9,  "Which method is called by the JVM to start a thread?",
                "start()", "run()", "execute()", "init()", 0),
            q(10, "What is the result of 5 >> 1 in Java?",
                "10", "2", "3", "1", 1),
            q(11, "Which Java version introduced records?",
                "Java 11", "Java 14 (preview)", "Java 16 (stable)", "Java 17", 2),
            q(12, "What is autoboxing?",
                "Converting an array to a list", "Automatic conversion between primitives and wrappers",
                "Casting a subtype to supertype", "Wrapping exceptions automatically", 1),
            q(13, "Which annotation marks a method that overrides a superclass method?",
                "@Override", "@Inherited", "@SuppressWarnings", "@Deprecated", 0),
            q(14, "What is the purpose of the 'transient' keyword?",
                "Marks a field as thread-local", "Skips a field during serialization",
                "Makes a field immutable", "Declares a temporary variable", 1),
            q(15, "Which interface is implemented by all Java exceptions?",
                "Serializable", "Cloneable", "Throwable", "Error", 0),
            q(16, "What does Optional.orElseGet() accept?",
                "A default value", "A Supplier", "A Function", "A Predicate", 1),
            q(17, "Which Java stream operation is a terminal operation?",
                "filter()", "map()", "collect()", "sorted()", 2),
            q(18, "What is method hiding in Java?",
                "Overriding a static method", "Using private methods in subclasses",
                "Calling super methods", "Hiding fields with the same name", 0),
            q(19, "Which garbage collector is used by default in Java 11+?",
                "Serial GC", "Parallel GC", "G1 GC", "ZGC", 2),
            q(20, "What is the scope of a variable declared inside a try-with-resources statement?",
                "The entire method", "Only the try block", "The try and catch blocks", "The entire class", 1)
        );
    }

    // -------------------------------------------------------------------------
    // 2 · Spring Boot Essentials (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> springQuestions() {
        return List.of(
            q(1,  "Which annotation enables auto-configuration in Spring Boot?",
                "@EnableAutoConfiguration", "@SpringBootApplication", "@Configuration", "@ComponentScan", 1),
            q(2,  "What is the default embedded server in Spring Boot?",
                "Jetty", "Undertow", "Tomcat", "Netty", 2),
            q(3,  "Which annotation maps HTTP GET requests to a method?",
                "@PostMapping", "@RequestMapping", "@GetMapping", "@PutMapping", 2),
            q(4,  "What does @Transactional do by default on a RuntimeException?",
                "Commits the transaction", "Does nothing", "Rolls back the transaction",
                "Logs the exception", 2),
            q(5,  "Which file configures Spring Boot properties?",
                "beans.xml", "application.properties", "spring.xml", "config.json", 1),
            q(6,  "What annotation is used to inject a bean by type?",
                "@Inject", "@Resource", "@Autowired", "@Value", 2),
            q(7,  "Which Spring Data method returns an Optional?",
                "findAll()", "getById()", "findById()", "listAll()", 2),
            q(8,  "What HTTP status does @ResponseStatus(HttpStatus.CREATED) return?",
                "200", "201", "204", "202", 1),
            q(9,  "What is the purpose of @PathVariable?",
                "Maps a query parameter", "Maps a request body field",
                "Extracts a URI template variable", "Binds a header value", 2),
            q(10, "Which annotation declares a Spring bean explicitly in a config class?",
                "@Service", "@Bean", "@Component", "@Repository", 1),
            q(11, "What does spring.jpa.hibernate.ddl-auto=update do?",
                "Drops and recreates the schema", "Only validates the schema",
                "Updates the schema without dropping data", "Has no effect", 2),
            q(12, "Which scope instantiates a new bean for each HTTP request?",
                "singleton", "prototype", "request", "session", 2),
            q(13, "What does @ControllerAdvice allow you to do?",
                "Configure embedded server", "Define global exception handlers",
                "Enable caching", "Map multiple controllers", 1),
            q(14, "Which dependency adds Spring Security to a Boot project?",
                "spring-boot-starter-web", "spring-boot-starter-security",
                "spring-security-core", "spring-boot-security-auto", 1),
            q(15, "What does @RequestBody do?",
                "Sends a request", "Deserializes the HTTP body into a Java object",
                "Validates request params", "Sets response headers", 1),
            q(16, "Which annotation makes a class a JPA entity?",
                "@Table", "@Column", "@Entity", "@Repository", 2),
            q(17, "What is the default Spring Boot banner output location?",
                "System.err", "A log file", "System.out (console)",
                "A banner.txt HTTP endpoint", 2),
            q(18, "Which profile annotation activates a bean only in 'dev' profile?",
                "@ActiveProfiles(\"dev\")", "@Profile(\"dev\")", "@ConditionalOnProfile(\"dev\")",
                "@EnableProfile(\"dev\")", 1),
            q(19, "What does @SpringBootTest do?",
                "Runs unit tests only", "Loads the full application context for integration tests",
                "Mocks all beans", "Skips dependency injection", 1),
            q(20, "Which actuator endpoint exposes health information?",
                "/actuator/info", "/actuator/status", "/actuator/health", "/actuator/ping", 2)
        );
    }

    // -------------------------------------------------------------------------
    // 3 · JavaScript Essentials (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> jsQuestions() {
        return List.of(
            q(1,  "Which keyword declares a block-scoped variable in ES6?",
                "var", "let", "function", "type", 1),
            q(2,  "What does the spread operator (...) do?",
                "Declares a rest parameter", "Expands an iterable into individual elements",
                "Creates a deep copy", "Merges two objects recursively", 1),
            q(3,  "What does Promise.all() do?",
                "Resolves as soon as one promise resolves",
                "Resolves when all promises resolve",
                "Ignores rejected promises",
                "Runs promises sequentially", 1),
            q(4,  "What is the output of typeof null?",
                "\"null\"", "\"undefined\"", "\"object\"", "\"boolean\"", 2),
            q(5,  "Which array method returns a new transformed array?",
                "forEach()", "filter()", "map()", "reduce()", 2),
            q(6,  "What is a closure in JavaScript?",
                "A function with no return value",
                "A function that retains access to its outer scope",
                "An arrow function",
                "A self-invoking function", 1),
            q(7,  "What does async/await do?",
                "Creates a new thread",
                "Makes synchronous code run faster",
                "Syntactic sugar over Promises",
                "Blocks the event loop", 2),
            q(8,  "Which method adds an element to the end of an array?",
                "unshift()", "push()", "splice()", "concat()", 1),
            q(9,  "What is the difference between == and ===?",
                "No difference",
                "=== also checks type",
                "== checks strict equality",
                "=== converts types before comparing", 1),
            q(10, "Which statement imports a default export?",
                "import { foo } from './foo'", "import foo from './foo'",
                "require('./foo')", "import * from './foo'", 1),
            q(11, "What does JSON.parse() return?",
                "A JSON string", "A JavaScript object or primitive",
                "A Map object", "An array always", 1),
            q(12, "Which event fires when the DOM is fully loaded?",
                "load", "DOMContentLoaded", "ready", "init", 1),
            q(13, "What does the nullish coalescing operator (??) return?",
                "Left side if truthy", "Left side if not null/undefined",
                "Right side always", "Left side if false", 1),
            q(14, "Which method converts a JavaScript object to a JSON string?",
                "JSON.parse()", "JSON.stringify()", "Object.toString()", "JSON.encode()", 1),
            q(15, "What is hoisting?",
                "Moving imports to the top of the file",
                "The JS engine moving declarations to the top of their scope",
                "Lazy evaluation of expressions",
                "Prototype chain lookup", 1),
            q(16, "Which built-in object stores key-value pairs of any type as keys?",
                "Object", "WeakMap", "Map", "Set", 2),
            q(17, "What does Array.from() do?",
                "Converts an array to a string",
                "Creates a shallow copy from an array-like or iterable",
                "Flattens nested arrays",
                "Sorts an array", 1),
            q(18, "What is the purpose of the 'use strict' directive?",
                "Enables ES6 features",
                "Enforces stricter parsing and error handling",
                "Disables async functions",
                "Activates tree shaking", 1),
            q(19, "Which method removes the last element from an array?",
                "shift()", "pop()", "splice(-1)", "delete()", 1),
            q(20, "What does the optional chaining operator (?.) do?",
                "Throws if null", "Returns undefined instead of throwing on null/undefined",
                "Creates optional parameters", "Short-circuits boolean expressions", 1)
        );
    }

    // -------------------------------------------------------------------------
    // 4 · SQL & Relational Databases (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> sqlQuestions() {
        return List.of(
            q(1,  "Which SQL statement retrieves data from a table?",
                "INSERT", "UPDATE", "SELECT", "DELETE", 2),
            q(2,  "What does a PRIMARY KEY constraint guarantee?",
                "All values are positive", "Values are unique and not null",
                "Values reference another table", "Values are indexed", 1),
            q(3,  "Which JOIN returns all rows from both tables, with NULLs for missing matches?",
                "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL OUTER JOIN", 3),
            q(4,  "What does the GROUP BY clause do?",
                "Sorts the result set", "Filters grouped rows",
                "Groups rows with the same value in specified columns",
                "Joins two tables", 2),
            q(5,  "Which clause filters grouped results?",
                "WHERE", "HAVING", "ORDER BY", "FILTER", 1),
            q(6,  "What is a foreign key?",
                "A unique identifier for a row",
                "A column that references the primary key of another table",
                "An auto-incremented column",
                "An indexed column", 1),
            q(7,  "Which normal form eliminates partial dependencies?",
                "1NF", "2NF", "3NF", "BCNF", 1),
            q(8,  "What does ACID stand for?",
                "Atomicity, Consistency, Integrity, Durability",
                "Atomicity, Concurrency, Isolation, Durability",
                "Atomicity, Consistency, Isolation, Durability",
                "Availability, Consistency, Isolation, Durability", 2),
            q(9,  "Which command permanently removes a table and its data?",
                "DELETE", "TRUNCATE", "DROP", "REMOVE", 2),
            q(10, "What is an index used for?",
                "Enforcing uniqueness only", "Speeding up data retrieval",
                "Creating relationships", "Storing computed values", 1),
            q(11, "Which SQL function counts non-null values in a column?",
                "SUM()", "AVG()", "COUNT(column)", "COUNT(*)", 2),
            q(12, "What does DISTINCT do in a SELECT query?",
                "Orders results", "Removes duplicate rows from results",
                "Filters by condition", "Aggregates data", 1),
            q(13, "What is a stored procedure?",
                "A view that stores computations",
                "A precompiled set of SQL statements stored in the database",
                "A trigger that fires on INSERT",
                "An index on a function", 1),
            q(14, "Which isolation level prevents dirty reads?",
                "READ UNCOMMITTED", "READ COMMITTED",
                "REPEATABLE READ", "SERIALIZABLE", 1),
            q(15, "What does the EXPLAIN keyword do?",
                "Runs a query and explains the result",
                "Shows the query execution plan",
                "Validates SQL syntax",
                "Displays table schema", 1),
            q(16, "Which constraint ensures a column value is never NULL?",
                "UNIQUE", "CHECK", "NOT NULL", "DEFAULT", 2),
            q(17, "What is a self-join?",
                "Joining a table with a temporary table",
                "Joining a table with itself",
                "A join without a ON condition",
                "A join on the same column", 1),
            q(18, "Which keyword renames a column alias in a SELECT?",
                "RENAME", "AS", "ALIAS", "NAME", 1),
            q(19, "What does ROLLBACK do?",
                "Saves all changes permanently", "Undoes all changes in the current transaction",
                "Creates a savepoint", "Drops the last inserted row", 1),
            q(20, "What is the purpose of a composite key?",
                "To speed up queries", "A primary key made of two or more columns",
                "A key that spans multiple tables", "An auto-generated UUID key", 1)
        );
    }

    // -------------------------------------------------------------------------
    // 5 · REST API Design (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> restQuestions() {
        return List.of(
            q(1,  "Which HTTP method is idempotent and returns a resource?",
                "POST", "PUT", "GET", "PATCH", 2),
            q(2,  "What HTTP status code means 'resource created'?",
                "200", "201", "204", "202", 1),
            q(3,  "What does '404 Not Found' mean?",
                "Server error", "The requested resource does not exist",
                "Unauthorized access", "Request timeout", 1),
            q(4,  "Which HTTP method is used for a full update of a resource?",
                "POST", "PATCH", "GET", "PUT", 3),
            q(5,  "What is HATEOAS?",
                "A caching strategy", "An authentication protocol",
                "Including navigational links in API responses",
                "A compression algorithm", 2),
            q(6,  "Which status code indicates a successful request with no body?",
                "200", "201", "204", "202", 2),
            q(7,  "What does the 'Authorization' header typically carry?",
                "The API version", "A Bearer token or Basic credentials",
                "The content type", "The request origin", 1),
            q(8,  "What is the purpose of API versioning?",
                "Caching responses", "Throttling clients",
                "Avoiding breaking changes for existing clients",
                "Compressing payloads", 2),
            q(9,  "Which format is most commonly used for REST API responses?",
                "XML", "CSV", "JSON", "YAML", 2),
            q(10, "What does 401 Unauthorized mean?",
                "The user is authenticated but lacks permission",
                "Authentication is required or has failed",
                "The resource is permanently unavailable",
                "The server is down", 1),
            q(11, "What is the difference between 401 and 403?",
                "No difference",
                "401 = not authenticated, 403 = not authorized",
                "401 = server error, 403 = client error",
                "401 = timeout, 403 = not found", 1),
            q(12, "What does a 429 status code indicate?",
                "Server error", "Too many requests (rate limiting)",
                "Payload too large", "Unsupported media type", 1),
            q(13, "Which HTTP method should be used to partially update a resource?",
                "PUT", "POST", "PATCH", "DELETE", 2),
            q(14, "What is idempotency?",
                "Making multiple identical requests produces the same result as one",
                "A request that never fails",
                "A cached response",
                "A stateless request", 0),
            q(15, "What does CORS stand for?",
                "Cross-Origin Resource Sharing", "Cross-Origin Request Security",
                "Client-Origin Response Specification", "Content-Origin Request Scheme", 0),
            q(16, "What is the purpose of an ETag header?",
                "Specifies the encoding", "Identifies a specific version of a resource for caching",
                "Sets the content type", "Authorizes the request", 1),
            q(17, "Which constraint makes REST stateless?",
                "Uniform interface", "Stateless constraint — each request contains all info needed",
                "Layered system", "Code on demand", 1),
            q(18, "What is the correct content-type for a JSON request body?",
                "text/json", "application/xml", "application/json", "text/plain", 2),
            q(19, "Which HTTP method deletes a resource?",
                "REMOVE", "DELETE", "PURGE", "CLEAR", 1),
            q(20, "What does a 500 Internal Server Error indicate?",
                "The client sent an invalid request",
                "The resource was not found",
                "An unexpected condition on the server",
                "Rate limit exceeded", 2)
        );
    }

    // -------------------------------------------------------------------------
    // 6 · Python Basics (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> pythonQuestions() {
        return List.of(
            q(1,  "Which keyword defines a function in Python?",
                "function", "fun", "def", "lambda", 2),
            q(2,  "What data type is immutable and ordered in Python?",
                "list", "dict", "tuple", "set", 2),
            q(3,  "What does len([1,2,3]) return?",
                "2", "3", "4", "1", 1),
            q(4,  "Which method adds an element to the end of a list?",
                "add()", "insert()", "append()", "push()", 2),
            q(5,  "What does *args allow in a function?",
                "Keyword arguments", "A variable number of positional arguments",
                "Default arguments", "Type-annotated arguments", 1),
            q(6,  "What is a list comprehension?",
                "A way to sort lists",
                "A concise syntax to create lists from iterables",
                "A built-in sorting algorithm",
                "A method to filter duplicates", 1),
            q(7,  "Which keyword is used for exception handling in Python?",
                "catch", "except", "handle", "rescue", 1),
            q(8,  "What does the 'with' statement do?",
                "Reimports a module", "Creates a context manager ensuring resource cleanup",
                "Starts a loop", "Defines a nested function", 1),
            q(9,  "What is the output of bool('') in Python?",
                "True", "False", "None", "Error", 1),
            q(10, "Which built-in function returns the type of an object?",
                "class()", "typeof()", "type()", "isinstance()", 2),
            q(11, "What does a decorator do in Python?",
                "Compiles a function", "Wraps a function to modify its behavior",
                "Inherits from a base class", "Creates a singleton", 1),
            q(12, "What is the use of __init__ in a class?",
                "Destroys the object", "Imports the module",
                "Initializes instance attributes", "Defines class methods", 2),
            q(13, "Which module is used for regular expressions in Python?",
                "regex", "re", "match", "string", 1),
            q(14, "What does range(1, 5) produce?",
                "1, 2, 3, 4, 5", "1, 2, 3, 4", "0, 1, 2, 3, 4", "1, 2, 3, 4, 5, 6", 1),
            q(15, "Which statement exits a loop prematurely?",
                "exit", "return", "break", "stop", 2),
            q(16, "What is a generator in Python?",
                "A function that returns a list",
                "A function that yields values lazily using 'yield'",
                "A class that inherits from Iterator",
                "A built-in data type", 1),
            q(17, "Which keyword checks membership in a collection?",
                "has", "contains", "in", "within", 2),
            q(18, "What does dict.get(key, default) do?",
                "Raises KeyError if key is missing",
                "Returns default if key is not found",
                "Inserts key with default value",
                "Updates the key", 1),
            q(19, "What is PEP 8?",
                "Python's error handling specification",
                "The Python style guide",
                "The Python security standard",
                "A packaging specification", 1),
            q(20, "What does the zip() function do?",
                "Compresses a file", "Combines two iterables into pairs of tuples",
                "Flattens a nested list", "Sorts two lists together", 1)
        );
    }

    // -------------------------------------------------------------------------
    // 7 · Git & Version Control (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> gitQuestions() {
        return List.of(
            q(1,  "Which command initializes a new Git repository?",
                "git start", "git init", "git create", "git new", 1),
            q(2,  "What does 'git clone' do?",
                "Creates a new branch", "Copies a remote repository locally",
                "Merges two branches", "Rebases commits", 1),
            q(3,  "Which command stages all changes?",
                "git commit -a", "git stage .", "git add .", "git push --all", 2),
            q(4,  "What does 'git stash' do?",
                "Deletes uncommitted changes",
                "Temporarily shelves changes not ready to commit",
                "Pushes changes to remote",
                "Creates a tag", 1),
            q(5,  "What is a fast-forward merge?",
                "A merge with a new merge commit",
                "Moving a branch pointer forward with no diverging history",
                "Rebasing before merging",
                "A squash merge", 1),
            q(6,  "Which command shows the commit history?",
                "git status", "git diff", "git log", "git show", 2),
            q(7,  "What does 'git rebase' do?",
                "Reverts a commit", "Replays commits on top of another branch",
                "Merges branches with a merge commit", "Squashes commits into tags", 1),
            q(8,  "What is the purpose of .gitignore?",
                "Tags ignored commits", "Specifies intentionally untracked files to ignore",
                "Marks files as read-only", "Excludes branches from pushes", 1),
            q(9,  "Which command undoes the last commit but keeps changes staged?",
                "git revert HEAD", "git reset --soft HEAD~1",
                "git reset --hard HEAD~1", "git checkout HEAD~1", 1),
            q(10, "What does 'git cherry-pick' do?",
                "Deletes a branch", "Applies a specific commit to the current branch",
                "Merges all commits from a branch", "Tags a commit", 1),
            q(11, "What is 'origin' in Git?",
                "The main branch", "A default alias for the remote repository URL",
                "The first commit", "The local staging area", 1),
            q(12, "Which command creates and switches to a new branch?",
                "git branch new-branch", "git checkout new-branch",
                "git checkout -b new-branch", "git switch --create new-branch", 2),
            q(13, "What does 'git fetch' do?",
                "Fetches and merges remote changes",
                "Downloads remote changes without merging",
                "Pushes local commits",
                "Resets the working directory", 1),
            q(14, "What is a Git tag used for?",
                "Marking a branch as protected",
                "Marking a specific commit (e.g., a release)",
                "Storing credentials",
                "Filtering the commit log", 1),
            q(15, "Which merge strategy preserves all individual commits?",
                "--squash", "--no-ff", "--ff-only", "--rebase", 1),
            q(16, "What does 'git diff' show?",
                "The commit history", "Differences between working tree and index",
                "Remote tracking branches", "Staged files only", 1),
            q(17, "What command pushes a local branch to a remote?",
                "git push origin branch-name", "git upload origin branch-name",
                "git send origin branch-name", "git push --remote branch-name", 0),
            q(18, "What is a pull request (PR)?",
                "A command that pulls and compresses changes",
                "A request to merge changes, reviewed before integrating",
                "An automated CI pipeline",
                "A git hook that runs tests", 1),
            q(19, "What does 'git bisect' help with?",
                "Finding merge conflicts",
                "Binary search through commits to find which one introduced a bug",
                "Listing all branches",
                "Comparing two branches", 1),
            q(20, "What is the HEAD in Git?",
                "The latest tag", "A pointer to the currently checked-out commit/branch",
                "The remote origin", "The initial commit", 1)
        );
    }

    // -------------------------------------------------------------------------
    // 8 · HTML & CSS Fundamentals (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> htmlCssQuestions() {
        return List.of(
            q(1,  "Which HTML tag defines the document title shown in the browser tab?",
                "<header>", "<meta>", "<title>", "<h1>", 2),
            q(2,  "What does the 'alt' attribute on an <img> do?",
                "Sets the image size", "Provides alternative text for accessibility",
                "Adds a caption", "Links to another image", 1),
            q(3,  "What is the CSS box model composed of?",
                "Content, padding, border, margin",
                "Width, height, display, float",
                "Block, inline, flex, grid",
                "Position, z-index, overflow, visibility", 0),
            q(4,  "Which CSS property controls the stacking order of elements?",
                "position", "z-index", "overflow", "display", 1),
            q(5,  "What does 'display: flex' do?",
                "Makes the element invisible",
                "Enables a flex formatting context on the container",
                "Floats children to the left",
                "Creates a grid layout", 1),
            q(6,  "Which HTML5 element represents standalone content like a blog post?",
                "<section>", "<div>", "<article>", "<aside>", 2),
            q(7,  "What does the CSS 'position: absolute' property do?",
                "Removes the element from the normal flow, positioned relative to nearest positioned ancestor",
                "Fixes the element to the viewport",
                "Positions relative to itself",
                "Makes the element sticky on scroll", 0),
            q(8,  "What is a CSS media query used for?",
                "Fetching external stylesheets",
                "Applying styles based on device characteristics",
                "Querying the DOM",
                "Animating elements", 1),
            q(9,  "Which selector has the highest specificity?",
                "Element selector (div)", "Class selector (.class)",
                "ID selector (#id)", "Universal selector (*)", 2),
            q(10, "What does 'semantic HTML' mean?",
                "Using only inline styles",
                "Using tags that convey meaning about the content",
                "Minimizing the number of HTML elements",
                "Writing HTML without CSS", 1),
            q(11, "Which CSS property makes text bold?",
                "font-style", "text-weight", "font-weight", "text-bold", 2),
            q(12, "What attribute makes a form input required?",
                "mandatory", "validate", "required", "notempty", 2),
            q(13, "What does 'box-sizing: border-box' do?",
                "Includes padding and border in the element's total width and height",
                "Removes all margins",
                "Makes the element a block",
                "Sets a default border", 0),
            q(14, "Which HTML element creates a hyperlink?",
                "<link>", "<a>", "<href>", "<nav>", 1),
            q(15, "What is the purpose of the <meta charset='UTF-8'> tag?",
                "Sets the viewport width", "Specifies the character encoding of the document",
                "Links a favicon", "Defines the author", 1),
            q(16, "Which CSS unit is relative to the root element's font size?",
                "em", "px", "rem", "%", 2),
            q(17, "What does 'justify-content: space-between' do in flexbox?",
                "Centers all items", "Adds equal space around each item",
                "Distributes items with space between them and no space at the edges",
                "Aligns items to the cross axis", 2),
            q(18, "What is the default value of the 'position' CSS property?",
                "relative", "absolute", "fixed", "static", 3),
            q(19, "Which tag is used to embed JavaScript in HTML?",
                "<js>", "<javascript>", "<script>", "<code>", 2),
            q(20, "What does CSS Grid's 'fr' unit represent?",
                "Font rem", "A fraction of the available space in the grid container",
                "Fixed row height", "Full-width column", 1)
        );
    }

    // -------------------------------------------------------------------------
    // 9 · Agile & Scrum (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> agileQuestions() {
        return List.of(
            q(1,  "What are the three roles in Scrum?",
                "Manager, Developer, Tester",
                "Product Owner, Scrum Master, Development Team",
                "Product Owner, Tech Lead, QA",
                "Project Manager, Scrum Master, Developer", 1),
            q(2,  "What is a Sprint?",
                "A daily standup meeting",
                "A time-boxed iteration (usually 1-4 weeks)",
                "The set of all backlog items",
                "A retrospective meeting", 1),
            q(3,  "What is the Product Backlog?",
                "A list of bugs only",
                "An ordered list of everything that might be done in the product",
                "The Sprint goal",
                "A burndown chart", 1),
            q(4,  "What is the purpose of the Daily Scrum?",
                "Sprint planning", "Reviewing the product increment",
                "A 15-minute sync to inspect progress and plan the next 24 hours",
                "Retrospective on the last Sprint", 2),
            q(5,  "What does the Scrum Master do?",
                "Writes code", "Manages the team's salaries",
                "Serves the team by removing impediments and facilitating Scrum",
                "Owns the product vision", 2),
            q(6,  "What is the Sprint Review?",
                "An internal code review",
                "A meeting to inspect the increment and adapt the backlog",
                "A security audit",
                "A performance review of team members", 1),
            q(7,  "What is velocity in Scrum?",
                "The number of bugs fixed per Sprint",
                "The number of lines of code per day",
                "The amount of work a team completes in a Sprint",
                "The speed of deployment", 2),
            q(8,  "What is a burndown chart?",
                "A chart showing team morale",
                "A visual representation of remaining work vs. time in a Sprint",
                "A chart of deployment frequency",
                "A diagram of system architecture", 1),
            q(9,  "What is the Definition of Done (DoD)?",
                "A checklist for the daily standup",
                "A shared understanding of what 'complete' means for an increment",
                "The sprint planning agenda",
                "The product owner's acceptance criteria", 1),
            q(10, "Which Agile principle values 'Working software over comprehensive documentation'?",
                "Lean principle", "Kanban rule",
                "Agile Manifesto", "PRINCE2 guideline", 2),
            q(11, "What is a user story?",
                "A technical specification",
                "An informal description of a feature from the end user's perspective",
                "A bug report",
                "A deployment script", 1),
            q(12, "What is Kanban?",
                "A Scrum ceremony",
                "A visual workflow management method using a board and cards",
                "A type of sprint",
                "A project management certification", 1),
            q(13, "What is the Sprint Retrospective for?",
                "Reviewing the product with stakeholders",
                "Planning the next sprint backlog",
                "Reflecting on the process to improve in the next Sprint",
                "Demoing the increment", 2),
            q(14, "What is a Story Point?",
                "A unit of time (hours)",
                "A relative unit of effort/complexity for estimation",
                "A line in the product backlog",
                "A test case", 1),
            q(15, "What does WIP limit mean in Kanban?",
                "Weekly iteration planning",
                "Maximum number of items allowed in a workflow stage simultaneously",
                "Work in progress log",
                "Weight of individual processes", 1),
            q(16, "What is an Epic?",
                "A completed sprint",
                "A large user story that can be broken into smaller stories",
                "A retrospective finding",
                "The entire product backlog", 1),
            q(17, "Who is responsible for prioritizing the Product Backlog?",
                "Scrum Master", "Development Team", "Product Owner", "Stakeholders", 2),
            q(18, "What is 'pair programming'?",
                "Two developers working on separate features",
                "Two developers collaborating on the same code at one workstation",
                "A code review performed by a pair",
                "Merging two feature branches", 1),
            q(19, "What is Continuous Integration (CI)?",
                "Deploying to production continuously",
                "Merging code changes frequently with automated build and test",
                "Writing tests after code",
                "A Scrum ceremony", 1),
            q(20, "What does 'timeboxing' mean in Agile?",
                "Setting a fixed maximum time for an activity",
                "Estimating story points using time",
                "Tracking hours per developer",
                "Deadlines imposed by managers", 0)
        );
    }

    // -------------------------------------------------------------------------
    // 10 · Cybersecurity Basics (20 questions)
    // -------------------------------------------------------------------------

    private List<Question> cyberQuestions() {
        return List.of(
            q(1,  "What does SQL injection exploit?",
                "Weak passwords", "Unsanitized user input inserted into SQL queries",
                "Outdated SSL certificates", "CSRF tokens", 1),
            q(2,  "What is Cross-Site Scripting (XSS)?",
                "Injecting SQL into a database",
                "Injecting malicious scripts into web pages viewed by others",
                "Intercepting HTTP traffic",
                "Brute-forcing passwords", 1),
            q(3,  "What does HTTPS provide over HTTP?",
                "Faster page loads", "Encryption and server authentication using TLS",
                "Compressed responses", "Stateless sessions", 1),
            q(4,  "What is a CSRF attack?",
                "Injecting scripts into a page",
                "Forging requests on behalf of an authenticated user from another site",
                "Intercepting cookies",
                "Exploiting SQL queries", 1),
            q(5,  "What is the purpose of hashing passwords?",
                "Encrypting them for transmission",
                "Storing a one-way transformation so the plain password is never stored",
                "Compressing them to save space",
                "Converting them to base64", 1),
            q(6,  "What is a Man-in-the-Middle (MITM) attack?",
                "Overloading a server with requests",
                "Intercepting communication between two parties without their knowledge",
                "Injecting code into a database",
                "Phishing via email", 1),
            q(7,  "What is the principle of least privilege?",
                "Giving all users admin rights for efficiency",
                "Granting users only the permissions they need to perform their tasks",
                "Encrypting all data at rest",
                "Using MFA everywhere", 1),
            q(8,  "What does a JWT (JSON Web Token) consist of?",
                "Username and password in base64",
                "Header, payload, and signature",
                "Session ID and expiry",
                "An asymmetric key pair", 1),
            q(9,  "What is a brute-force attack?",
                "Guessing credentials by systematically trying all possibilities",
                "Injecting malicious SQL",
                "Intercepting network packets",
                "Exploiting an XSS vulnerability", 0),
            q(10, "What is two-factor authentication (2FA)?",
                "Two passwords entered sequentially",
                "A second layer of verification in addition to a password",
                "Biometric login only",
                "A VPN-based login", 1),
            q(11, "What does 'encryption at rest' mean?",
                "Encrypting data during transmission",
                "Encrypting stored data so it is unreadable without the key",
                "Hashing passwords",
                "Using HTTPS", 1),
            q(12, "What is a DDoS attack?",
                "Injecting malicious code into a server",
                "Flooding a server with traffic from multiple sources to make it unavailable",
                "Stealing cookies",
                "Intercepting DNS queries", 1),
            q(13, "What is the purpose of a Content Security Policy (CSP) header?",
                "Controlling browser caching",
                "Preventing XSS by specifying allowed content sources",
                "Encrypting cookies",
                "Limiting request size", 1),
            q(14, "Which hashing algorithm is currently recommended for passwords?",
                "MD5", "SHA-1", "bcrypt / Argon2", "SHA-256", 2),
            q(15, "What does 'secure' flag on a cookie do?",
                "Makes the cookie HTTP-only",
                "Ensures the cookie is only sent over HTTPS connections",
                "Encrypts the cookie value",
                "Prevents the cookie from expiring", 1),
            q(16, "What is a security audit?",
                "Automated penetration testing",
                "A systematic evaluation of a system's security posture",
                "A log of all login attempts",
                "A firewall rule review", 1),
            q(17, "What is an OWASP Top 10?",
                "Ten best coding practices",
                "A list of the ten most critical web application security risks",
                "Ten security tools",
                "Ten encryption algorithms", 1),
            q(18, "What is rate limiting used for?",
                "Speeding up API responses",
                "Controlling how many requests a client can make in a time period",
                "Encrypting API keys",
                "Validating JWT tokens", 1),
            q(19, "What does input validation prevent?",
                "Slow queries", "Unauthorized data being processed by the application",
                "Memory leaks", "SSL errors", 1),
            q(20, "What is a penetration test (pen test)?",
                "A performance load test",
                "An authorized simulated attack to find vulnerabilities",
                "A code review for security bugs",
                "A network bandwidth test", 1)
        );
    }
}
