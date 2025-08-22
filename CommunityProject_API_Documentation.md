# CommunityProject API Documentation

## Overview
This document describes the complete CRUD (Create, Read, Update, Delete) operations for CommunityProject entities in the Origin Group Backend API.

## Base URL
```
/api/community-projects
```

## Authentication
All endpoints require proper authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Data Models

### CommunityProject Entity
```json
{
  "id": "uuid",
  "fullName": "string",
  "profession": "string",
  "email": "string",
  "phone": "string",
  "linkedIn": "string",
  "projectPhoto": "string (URL)",
  "projectName": "string",
  "category": "string",
  "location": "string",
  "description": "string",
  "status": "PENDING | APPROVED | REJECTED",
  "user": {
    "id": "uuid",
    "name": "string"
  },
  "team": [
    {
      "title": "string",
      "number": "integer"
    }
  ]
}
```

### CommunityDto (for creation)
```json
{
  "fullName": "string (required)",
  "profession": "string (required)",
  "email": "string (required)",
  "phone": "string (required)",
  "linkedIn": "string (required)",
  "projectName": "string (required)",
  "category": "string (required)",
  "location": "string (required)",
  "description": "string (required)",
  "team": [
    {
      "title": "string",
      "number": "integer"
    }
  ]
}
```

## API Endpoints

### 1. Create Community Project
**POST** `/api/community-projects`

Creates a new community project with photo upload.

**Request:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `userId`: UUID (required) - ID of the user creating the project
  - `project`: JSON string (required) - Project data
  - `projectPhoto`: File (required) - Project photo

**Response:**
- Status: `201 Created`
- Body: `CommunityProject` object

**Example:**
```bash
curl -X POST "http://localhost:8080/api/community-projects" \
  -H "Authorization: Bearer <token>" \
  -F "userId=123e4567-e89b-12d3-a456-426614174000" \
  -F "project={\"fullName\":\"John Doe\",\"profession\":\"Developer\",...}" \
  -F "projectPhoto=@photo.jpg"
```

### 2. Get Project by ID
**GET** `/api/community-projects/{id}`

Retrieves a specific community project by its ID.

**Response:**
- Status: `200 OK`
- Body: `CommunityProject` object

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer <token>"
```

### 3. Get All Projects
**GET** `/api/community-projects`

Retrieves all community projects.

**Response:**
- Status: `200 OK`
- Body: Array of `CommunityProject` objects

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects" \
  -H "Authorization: Bearer <token>"
```

### 4. Update Project
**PUT** `/api/community-projects/{id}`

Updates an existing community project.

**Request:**
- Content-Type: `application/json`
- Body: `CommunityProject` object

**Response:**
- Status: `200 OK`
- Body: Updated `CommunityProject` object

**Example:**
```bash
curl -X PUT "http://localhost:8080/api/community-projects/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Updated Name","profession":"Updated Profession",...}'
```

### 5. Delete Project
**DELETE** `/api/community-projects/{id}`

Deletes a community project.

**Response:**
- Status: `204 No Content`

**Example:**
```bash
curl -X DELETE "http://localhost:8080/api/community-projects/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer <token>"
```

### 6. Get Projects by User
**GET** `/api/community-projects/user/{userId}`

Retrieves all projects created by a specific user.

**Response:**
- Status: `200 OK`
- Body: Array of `CommunityProject` objects

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/user/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer <token>"
```

### 7. Get Projects by Status
**GET** `/api/community-projects/status/{status}`

Retrieves all projects with a specific status.

**Parameters:**
- `status`: `PENDING`, `APPROVED`, or `REJECTED`

**Response:**
- Status: `200 OK`
- Body: Array of `CommunityProject` objects

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/status/PENDING" \
  -H "Authorization: Bearer <token>"
```

### 8. Get Projects by Category
**GET** `/api/community-projects/category/{category}`

Retrieves all projects in a specific category.

**Response:**
- Status: `200 OK`
- Body: Array of `CommunityProject` objects

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/category/Technology" \
  -H "Authorization: Bearer <token>"
```

### 9. Get Projects by Location
**GET** `/api/community-projects/location/{location}`

Retrieves all projects from a specific location.

**Response:**
- Status: `200 OK`
- Body: Array of `CommunityProject` objects

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/location/New York" \
  -H "Authorization: Bearer <token>"
```

### 10. Search Projects by Name
**GET** `/api/community-projects/search?projectName={name}`

Searches for projects by name (case-insensitive).

**Response:**
- Status: `200 OK`
- Body: Array of `CommunityProject` objects

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/search?projectName=AI" \
  -H "Authorization: Bearer <token>"
```

### 11. Update Project Status
**PUT** `/api/community-projects/{id}/status?status={status}`

Updates the status of a specific project.

**Parameters:**
- `status`: `PENDING`, `APPROVED`, or `REJECTED`

**Response:**
- Status: `200 OK`
- Body: Updated `CommunityProject` object

**Example:**
```bash
curl -X PUT "http://localhost:8080/api/community-projects/123e4567-e89b-12d3-a456-426614174000/status?status=APPROVED" \
  -H "Authorization: Bearer <token>"
```

### 12. Add Team Member
**POST** `/api/community-projects/{id}/team-members`

Adds a new team member to a project.

**Request:**
- Content-Type: `application/json`
- Body: `TeamMember` object

**Response:**
- Status: `200 OK`
- Body: Updated `CommunityProject` object

**Example:**
```bash
curl -X POST "http://localhost:8080/api/community-projects/123e4567-e89b-12d3-a456-426614174000/team-members" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Developer","number":2}'
```

### 13. Remove Team Member
**DELETE** `/api/community-projects/{id}/team-members/{index}`

Removes a team member from a project by index.

**Response:**
- Status: `200 OK`
- Body: Updated `CommunityProject` object

**Example:**
```bash
curl -X DELETE "http://localhost:8080/api/community-projects/123e4567-e89b-12d3-a456-426614174000/team-members/0" \
  -H "Authorization: Bearer <token>"
```

### 14. Update Team Member
**PUT** `/api/community-projects/{id}/team-members/{index}`

Updates a team member at a specific index.

**Request:**
- Content-Type: `application/json`
- Body: `TeamMember` object

**Response:**
- Status: `200 OK`
- Body: Updated `CommunityProject` object

**Example:**
```bash
curl -X PUT "http://localhost:8080/api/community-projects/123e4567-e89b-12d3-a456-426614174000/team-members/0" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Senior Developer","number":1}'
```

### 15. Get Project Count
**GET** `/api/community-projects/count`

Returns the total number of projects.

**Response:**
- Status: `200 OK`
- Body: `long` (number)

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/count" \
  -H "Authorization: Bearer <token>"
```

### 16. Get Project Count by Status
**GET** `/api/community-projects/count/status/{status}`

Returns the number of projects with a specific status.

**Response:**
- Status: `200 OK`
- Body: `long` (number)

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/count/status/PENDING" \
  -H "Authorization: Bearer <token>"
```

### 17. Check Project Exists
**GET** `/api/community-projects/exists/{id}`

Checks if a project exists.

**Response:**
- Status: `200 OK`
- Body: `boolean`

**Example:**
```bash
curl -X GET "http://localhost:8080/api/community-projects/exists/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer <token>"
```

## Error Responses

All endpoints return standardized error responses:

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Project not found with id: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/community-projects/123e4567-e89b-12d3-a456-426614174000"
}
```

### Common HTTP Status Codes
- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `204 No Content`: Resource deleted successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Validation Rules

### CommunityProject DTO Validation
- `fullName`: Required, not blank
- `profession`: Required, not blank
- `email`: Required, valid email format
- `phone`: Required, not blank
- `linkedIn`: Required, not blank
- `projectName`: Required, not blank
- `category`: Required, not blank
- `location`: Required, not blank
- `description`: Required, not blank
- `team`: Required, list of TeamMember objects

### TeamMember Validation
- `title`: Required, not blank
- `number`: Required, positive integer

## Security Considerations

1. **Authentication**: All endpoints require valid JWT tokens
2. **Authorization**: Users can only modify their own projects (implemented in service layer)
3. **Input Validation**: All inputs are validated using Bean Validation
4. **File Upload**: Project photos are validated and stored securely via Cloudinary
5. **SQL Injection**: Protected by JPA/Hibernate parameterized queries
6. **XSS Protection**: Input sanitization and proper content types

## Performance Considerations

1. **Pagination**: Consider implementing pagination for large result sets
2. **Caching**: Implement caching for frequently accessed data
3. **Database Indexing**: Ensure proper indexes on frequently queried fields
4. **Lazy Loading**: Used for user relationships to improve performance

## Testing

The API can be tested using:
- Postman
- cURL commands
- Unit tests (recommended)
- Integration tests (recommended)

## Future Enhancements

1. **Pagination**: Add pagination support for list endpoints
2. **Filtering**: Add advanced filtering options
3. **Sorting**: Add sorting capabilities
4. **Bulk Operations**: Add bulk create/update/delete operations
5. **Audit Trail**: Add audit logging for all operations
6. **Rate Limiting**: Implement rate limiting for API endpoints
