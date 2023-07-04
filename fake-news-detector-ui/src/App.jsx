import {useState} from 'react'
import './App.css'
import {api} from "./dal/api.js";

function App() {
    const [text, setText] = useState('');
    const [result, setResult] = useState('')

    const handleInputChange = (e) => {
        setText(e.target.value);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const {data} = await api.checkTruth({text})
            setResult(data.state)
            console.log('Response: ', data)
        } catch (error) {
            setResult('Server error')
            console.error('Network error:', error);
        }
    };

    return (
        <>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    value={text}
                    onChange={handleInputChange}
                    placeholder="Enter your post text"
                />
                <button type="submit">Submit</button>
            </form>

            {result && <div>Response result: {result}</div>}
        </>
    );
}

export default App
