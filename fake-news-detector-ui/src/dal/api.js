import axios from "axios";

const API_URL = 'http://localhost:10000/api/v1'

export const client = axios.create({
    baseURL: API_URL,
})

export const api = {
    checkTruth: async ({text}) => {
        const {data} = client.post('detector', {text})
        return data
    }
}