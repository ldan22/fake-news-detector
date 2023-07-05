import axios from "axios";

const API_URL = 'http://localhost:10000/api/v1'

export const client = axios.create({
    baseURL: API_URL,
})

export const api = {
    checkTruth: ({text, nlpProcessor = 'gpt'}) => {
        return client.post('detector', {text, nlpProcessor})
    }
}