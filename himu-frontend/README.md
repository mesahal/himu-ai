# Himu Chatbot Frontend

React frontend application for chatting with Himu, built with Vite and Tailwind CSS.

## Prerequisites

- Node.js 16 or higher
- npm or yarn

## Setup

### 1. Install Dependencies

```bash
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

The application will start on `http://localhost:5173`

### 3. Build for Production

```bash
npm run build
```

The built files will be in the `dist` directory.

## Configuration

Make sure the backend is running on `http://localhost:8080`. If your backend is running on a different port, update the API URL in `src/App.jsx`:

```javascript
const response = await fetch('http://localhost:8080/api/chat', {
  // ...
});
```

## Features

- Dark theme UI optimized for chat
- Real-time messaging with Himu
- Support for English, Bengali, and Banglish input
- Responsive design
- Loading states and error handling

## Project Structure

```
himu-frontend/
├── src/
│   ├── App.jsx          # Main chat component
│   ├── main.jsx         # React entry point
│   └── index.css        # Tailwind CSS styles
├── index.html           # HTML template
├── package.json         # Dependencies
├── vite.config.js       # Vite configuration
└── tailwind.config.js   # Tailwind configuration
```

## Technologies

- **React 18**: UI library
- **Vite**: Build tool and dev server
- **Tailwind CSS**: Utility-first CSS framework

## Development

The app uses Vite for fast development with HMR (Hot Module Replacement). Any changes to the code will automatically reflect in the browser.

