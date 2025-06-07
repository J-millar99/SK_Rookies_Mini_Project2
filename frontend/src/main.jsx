import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { user } from './redux/user';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import './index.css';
import './api/axios';

const theme = createTheme({
  palette: {
    primary: {
      main: '#FF6F0F',
    },
    secondary: {
      main: '#FFA500',
    },
    background: {
      default: '#f9f9f9',
    },
    info: {
      main: '#0288d1',
      light: '#03a9f4',
      dark: '#01579b',
      extraLight: '#b3e5fc',
    },
    error: {
      main: '#d32f2f',
      light: '#ef9a9a',
      dark: '#b71c1c',
      extraLight: '#ffebee',
    },
  },
  typography: {
    fontFamily: 'Pretendard, sans-serif',
  },
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Provider store={user}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </ThemeProvider>
    </Provider>
  </React.StrictMode>
);
