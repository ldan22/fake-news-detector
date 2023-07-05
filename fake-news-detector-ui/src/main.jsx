import React from 'react'
import ReactDOM from 'react-dom/client'
import App from "./App.jsx";
import {MantineProvider} from '@mantine/core';
import {QueryClient, QueryClientProvider} from "react-query";

const queryClient = new QueryClient();


ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <MantineProvider withGlobalStyles withNormalizeCSS>
            <QueryClientProvider client={queryClient}>
                <App/>
            </QueryClientProvider>
        </MantineProvider>
    </React.StrictMode>,
)
