# ShardingSphere LIKE Query Support

A solution for enabling LIKE queries on encrypted columns in Apache ShardingSphere with database sharding.

## Problem

Apache ShardingSphere's encryption feature doesn't support LIKE queries on encrypted data. Traditional encryption makes pattern matching impossible, which is a common requirement for search functionality in sharded databases.

## Solution

This project implements a **chunk-based search approach** that enables LIKE queries while maintaining data encryption:

1. **Data Storage**: Original data is encrypted normally + broken into 3-character chunks
2. **Chunk Encryption**: Each chunk is hashed and stored in a PostgreSQL array column
3. **Query Transformation**: LIKE queries are converted to PostgreSQL array containment operations (`@>`)
4. **Search Process**: Search patterns are chunked and matched against stored chunk arrays

## How It Works

```
Email: "john@example.com"
↓
Chunks: ["joh", "ohn", "hn@", "n@e", "@ex", "exa", "xam", "amp", "mpl", "ple", "le.", ".co", "com"]
↓
Hashed chunks stored in array column
↓
Search: "%john%" → chunks: ["joh", "ohn"] → PostgreSQL: column @> ?::text[]
```

## Architecture

- **CustomLikeEncryptor**: Generates searchable chunks from plaintext
- **CustomSQLRewriter**: Transforms LIKE queries to array containment operations  
- **ShardingSphereConfig**: Configures encryption rules for both normal and chunk columns
- **UserRepository**: Handles search operations with encrypted data

## Database Schema

```sql
CREATE TABLE users (
    username VARCHAR(255),
    email VARCHAR(255),        -- AES encrypted
    email_chunks TEXT[],       -- Searchable chunks array
    password VARCHAR(255)      -- AES encrypted
);

-- Recommended index for performance
CREATE INDEX idx_users_email_chunks ON users USING GIN (email_chunks);
```

## Quick Start

1. **Setup Database**:
```sql
CREATE DATABASE testdb;
-- Create table as shown above
```

2. **Configure Connection** (in `ShardingSphereConfig.java`):
```java
hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/testdb");
hikariConfig.setUsername("your_username");
hikariConfig.setPassword("your_password");
```

3. **Run Example**:
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## Usage

```java
// Create user with encrypted data
User user = new User();
user.setEmail("john@example.com");
userRepo.createUser(user);

// Search encrypted data with LIKE
List<User> results = userRepo.findByEmailLike("%john%");
```

## Key Features

**Sharding Compatible**: Works with ShardingSphere's distributed architecture
**Security Maintained**: Original data remains fully encrypted
**Performance Optimized**: Uses PostgreSQL GIN indexes for fast array searches
**Transparent Integration**: LIKE queries work as expected in application code

## Limitations

- Minimum effective search pattern: 3 characters
- Case-insensitive searches only
- Additional storage overhead for chunk arrays

## Dependencies

- Apache ShardingSphere 5.3.2
- PostgreSQL 42.5.0
- Java 11+

