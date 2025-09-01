# Blog API Usage Guide

## Creating a Blog with Photo (Multipart)

### Endpoint

```
POST /api/blogs/create
Content-Type: multipart/form-data
```

### Request Structure

The request must contain two parts:

1. **`blog`** (Text field): JSON string containing blog details
2. **`photo`** (File field): Image file (optional)

### Example Request

#### Using Postman:

1. Set method to `POST`
2. Set URL to `http://localhost:8080/api/blogs/create`
3. In Body tab, select `form-data`
4. Add two fields:
   - **Key**: `blog` (Type: Text)
   - **Value**: `{"title": "My Blog", "description": "Blog content", "status": "DRAFT"}`
   - **Key**: `photo` (Type: File)
   - **Value**: Select your image file

#### Using cURL:

```bash
curl -X POST http://localhost:8080/api/blogs/create \
  -F "blog={\"title\":\"My Blog\",\"description\":\"Blog content\",\"status\":\"DRAFT\"}" \
  -F "photo=@/path/to/image.jpg"
```

#### Using JavaScript:

```javascript
const formData = new FormData();
formData.append(
  "blog",
  JSON.stringify({
    title: "My Blog",
    description: "Blog content",
    status: "DRAFT",
  })
);
formData.append("photo", imageFile);

fetch("/api/blogs/create", {
  method: "POST",
  body: formData,
});
```

### Important Notes:

- The `blog` field must be a valid JSON string
- The `photo` field is optional
- Use the exact field names: `blog` and `photo`
- Ensure the photo is an image file (JPG, PNG, GIF, etc.)

## Creating a Blog without Photo (JSON only)

### Endpoint

```
POST /api/blogs/json
Content-Type: application/json
```

### Example Request:

```json
{
  "title": "My Blog",
  "description": "Blog content",
  "status": "DRAFT"
}
```

## Other Endpoints

- **GET** `/api/blogs` - Get all blogs
- **GET** `/api/blogs/{id}` - Get blog by ID
- **GET** `/api/blogs/published` - Get published blogs only
- **GET** `/api/blogs/status/{status}` - Get blogs by status
- **PUT** `/api/blogs/{id}` - Update blog
- **DELETE** `/api/blogs/{id}` - Delete blog
- **PATCH** `/api/blogs/{id}/status?status=PUBLISHED` - Update blog status
- **POST** `/api/blogs/{id}/photo` - Upload/update blog photo
