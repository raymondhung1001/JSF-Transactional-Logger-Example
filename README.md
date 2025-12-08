# JSF Todo Application

A full-featured Todo application built with JavaServer Faces (JSF) and Java EE technologies.

## Technology Stack

- **TomEE 8.0.15** - Java EE application server
- **Java EE 8** - Enterprise Java platform
- **JPA (EclipseLink)** - Java Persistence API with EclipseLink provider
- **H2 Database** - In-memory database
- **PrimeFaces 12.0.0** - JSF component library
- **Bootstrap 4.6.2** - CSS framework
- **Apache Shiro 1.13.0** - Security framework for authentication and authorization
- **Maven** - Build and dependency management
- **Log4j2 2.20.0** - Logging framework with transactional logging support

## Features

- ✅ **User Authentication & Authorization** - Secure login/logout using Apache Shiro with JPA-based realm
- ✅ **Multi-User Support** - Multiple users with isolated todo lists
- ✅ **CRUD Operations** - Create, read, update, and delete todos
- ✅ **Todo Management** - Mark todos as complete/incomplete
- ✅ **Modern UI** - Responsive design with PrimeFaces and Bootstrap 4
- ✅ **Session Management** - 30-minute session timeout with automatic session handling
- ✅ **Transactional Logging** - Comprehensive Log4j2 logging of all database operations within JTA transactions
- ✅ **Cache Control** - HTTP cache control filters to prevent browser caching of sensitive pages
- ✅ **CDI Integration** - Shiro realm integrated with CDI for dependency injection

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Default Users

The application comes with three default users:

| Username | Password | Full Name |
|----------|----------|-----------|
| admin    | admin123 | Administrator |
| user1    | user123  | User One |
| user2    | user123  | User Two |

## Building the Application

```bash
mvn clean package
```

## Running the Application

### Using TomEE Maven Plugin

```bash
mvn tomee:run
```

This will:
1. Download TomEE Plus (if not already present)
2. Start the TomEE server
3. Deploy the application
4. Make it available at `http://localhost:8080/jsf-todo-app`

### Hot Reload Support

The application is configured with **hot reload** capabilities for faster development:

#### Automatic Hot Reload

When running with `mvn tomee:run`, the application supports hot reload with the following behavior:

- **Java classes** (`.java` files): 
  - When you save a Java file, your IDE compiles it to `target/classes`
  - Run `mvn compile` or let your IDE auto-compile
  - TomEE will detect the updated WAR file and reload the application
  - **Note**: Full application reload may take a few seconds
- **XHTML pages**: Automatically reloaded when the WAR is updated
- **Resources** (CSS, JS, images): Automatically reloaded via resource synchronization
- **Configuration files**: Reloaded when the application is redeployed

The TomEE plugin uses `reloadOnUpdate=true` to automatically reload the application when changes are detected in the compiled classes or webapp resources.

#### Manual Reload

If automatic reload doesn't work, you can manually trigger a reload:
1. Save your changes
2. The server will detect changes and reload automatically
3. Refresh your browser

#### IDE Integration

For best hot reload experience with your IDE:

**IntelliJ IDEA:**
1. Run `mvn tomee:run` from the terminal or configure a Maven run configuration
2. Enable "Build project automatically" in Settings → Build, Execution, Deployment → Compiler
3. Make changes to Java files and save - they will auto-compile to `target/classes`
4. Run `mvn package` or wait for TomEE to detect changes (may require manual rebuild)
5. TomEE will reload the application when the WAR is updated
6. **Tip**: For faster iteration, use "Update classes and resources" (Ctrl+F9) then run `mvn package`

**Eclipse:**
1. Run `mvn tomee:run` from the terminal
2. Enable "Build Automatically" in Project menu
3. Changes to Java files will be automatically compiled to `target/classes`
4. Run `mvn package` to rebuild the WAR, or TomEE may auto-detect changes
5. TomEE will reload the application when changes are detected
6. **Tip**: Right-click project → Maven → Update Project if classes aren't being detected

**VS Code:**
1. Run `mvn tomee:run` from the integrated terminal
2. Install the Java Extension Pack for automatic compilation
3. Changes to Java files will be automatically compiled
4. Run `mvn package` to rebuild the WAR for TomEE to pick up changes
5. **Tip**: Use "Java: Clean Java Language Server Workspace" if compilation isn't working

#### What Gets Hot Reloaded

✅ **Automatically reloaded:**
- **Java classes** (`.java` files) - Compiled automatically and reloaded by TomEE
  - Managed beans (JSF/CDI beans)
  - Services (EJBs)
  - Models (JPA entities)
  - Filters and listeners
  - All classes in `src/main/java`
- XHTML pages (`.xhtml` files)
- JavaScript files (`.js` files)
- CSS files
- Images and static resources
- Configuration files (with some limitations)

⚠️ **May require server restart:**
- Changes to `persistence.xml` or database schema
- Changes to `shiro.ini` security configuration
- Major changes to `web.xml` (some changes may require restart)
- Adding/removing dependencies (requires rebuild and restart)
- Changes to class structure that affect JPA entity mappings (may need restart)

#### Troubleshooting Hot Reload

If hot reload isn't working for Java files:
1. **Ensure automatic compilation is enabled** in your IDE
2. **Check that classes are being compiled** - verify `target/classes` contains updated `.class` files
3. **Rebuild the WAR file** - run `mvn package` to update the WAR with new classes
4. **Verify TomEE is running** - ensure you're running with `mvn tomee:run` (not a standalone server)
5. **Check compilation errors** - fix any compilation errors that prevent classes from being built
6. **Check console output** - look for reload messages in the TomEE console
7. **Manual reload** - if needed, stop and restart: `Ctrl+C` then `mvn tomee:run` again

**Common Java Hot Reload Issues:**
- **Classes not updating**: Run `mvn clean package` to ensure a fresh build and WAR update
- **IDE not compiling**: Check IDE build settings and ensure output directory is `target/classes`
- **TomEE not detecting changes**: The WAR file must be rebuilt - run `mvn package` after making changes
- **Application not reloading**: Check TomEE logs for reload messages; may need manual restart
- **ClassLoader issues**: Some complex changes (like adding new methods to interfaces) may require a full restart

**For Faster Development:**
- Use your IDE's built-in application server integration if available (IntelliJ Ultimate, Eclipse with server plugins)
- Consider using DCEVM (Dynamic Code Evolution VM) or HotswapAgent for true hot reload without restart
- For quick iterations, keep a terminal open and run `mvn package` after each change

### Alternative: Manual Deployment

1. Build the WAR file:
   ```bash
   mvn clean package
   ```

2. Deploy the `target/jsf-todo-app.war` to your TomEE server

## Accessing the Application

Once the server is running, open your browser and navigate to:

```
http://localhost:8080/jsf-todo-app
```

You will be redirected to the login page. Use one of the default users to log in.

## Project Structure

```
jsf-todo-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/todo/
│   │   │       ├── bean/          # JSF managed beans
│   │   │       ├── filter/         # Servlet filters (cache control)
│   │   │       ├── model/          # JPA entities (User, Todo)
│   │   │       ├── realm/          # Apache Shiro realm (JpaRealm)
│   │   │       ├── service/        # EJB services (TodoService, UserService)
│   │   │       └── shiro/          # Shiro configuration and CDI integration
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   └── persistence.xml # JPA configuration
│   │   │   └── log4j2.xml         # Log4j2 configuration
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── faces-config.xml
│   │       │   ├── resources.xml  # TomEE datasource config
│   │       │   ├── shiro.ini      # Apache Shiro security configuration
│   │       │   └── web.xml
│   │       ├── index.xhtml
│   │       ├── login.xhtml
│   │       └── todo/
│   │           └── list.xhtml
│   └── pom.xml
└── README.md
```

## Database

The application uses H2 in-memory database. The database is automatically created when the application starts. Data is persisted during the application lifecycle but will be lost when the server is stopped.

To use a persistent database, modify `src/main/webapp/WEB-INF/resources.xml` to use a file-based H2 database:

```xml
JdbcUrl jdbc:h2:file:./data/todoDB;AUTO_SERVER=TRUE
```

## Security

The application uses **Apache Shiro** for authentication and authorization:

- **JPA Realm** - Custom realm (`JpaRealm`) that authenticates users against the database
- **CDI Integration** - Shiro realm is integrated with CDI for dependency injection of services
- **URL-Based Security** - Security rules defined in `WEB-INF/shiro.ini`
- **Session Management** - 30-minute session timeout configured in `web.xml`
- **Protected Routes** - All todo pages require authentication; login and public resources are accessible anonymously

### Security Configuration

- Login page: `/login.xhtml` (anonymous access)
- Success URL after login: `/todo/list.xhtml`
- Protected URLs: `/todo/**` and all other routes require authentication
- Public resources: `/resources/**` and JSF resources are publicly accessible

## Logging

The application uses **Log4j2** for comprehensive transactional logging:

- **Transactional Logging** - All database operations (create, update, delete) are logged within JTA transactions
- **Log Levels** - Application uses DEBUG level for detailed operations, INFO for important events
- **Log Outputs**:
  - Console (standard output)
  - File: `logs/todo-app.log` (in TomEE logs directory)

Log4j2 configuration can be found in `src/main/resources/log4j2.xml`.

### Logged Operations

- User authentication attempts (success and failure)
- Todo CRUD operations (create, read, update, delete)
- User management operations
- Shiro security events
- Database transaction boundaries

## Development

### Adding New Users

Users can be added programmatically through the `UserService` EJB. The application initializes with three default users (see [Default Users](#default-users) section above).

Example:
```java
@Inject
private UserService userService;

public void createNewUser() {
    userService.createUser("newuser", "password123", "newuser@example.com", "New User");
}
```

### Customizing Security

- Shiro configuration: `src/main/webapp/WEB-INF/shiro.ini`
- Security realm: `src/main/java/com/example/todo/realm/JpaRealm.java`
- URL patterns and authentication rules can be modified in `shiro.ini`

### Customizing the UI

- JSF pages are located in `src/main/webapp/`
- Bootstrap 4 styles can be customized in the `<style>` sections of each page
- PrimeFaces components can be configured in `web.xml` context parameters
- Session timeout handling: `src/main/webapp/resources/js/session-timeout.js`

### Cache Control

The application includes cache control filters to prevent browser caching of sensitive pages:
- `CacheControlFilter` - Sets appropriate HTTP headers
- `NoCacheResponseWrapper` - Wraps responses to add no-cache headers

## Troubleshooting

### Port Already in Use

If port 8080 is already in use, you can change it in the TomEE configuration or use:
```bash
mvn tomee:run -Dtomee.httpPort=8081
```

### Database Connection Issues

Ensure the H2 database driver is properly configured in `resources.xml` and that the datasource name matches in `persistence.xml`.

### JSF Pages Not Loading

Check that:
1. The Faces Servlet is properly configured in `web.xml`
2. All JSF dependencies are included in `pom.xml`
3. The application is deployed correctly

### Authentication Issues

If you cannot log in:
1. Verify the database is initialized with default users (check `META-INF/data.sql`)
2. Check that `JpaRealm` is properly configured in `shiro.ini`
3. Ensure CDI is working correctly (check logs for Shiro initialization messages)
4. Verify `UserService` is accessible via JNDI or CDI

### Shiro Configuration Issues

If Shiro is not working:
1. Check `WEB-INF/shiro.ini` for correct configuration
2. Verify `ShiroEnvironmentLoaderListener` is registered in `web.xml`
3. Check application logs for Shiro initialization errors
4. Ensure `JpaRealm` is properly injected via CDI

## License

This is an example application for educational purposes.

