import {Button, TextInput} from "@mantine/core";
import {useState} from "react";

const KBExplorer = () => {
    const [text, setText] = useState('');
    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log({text})
    };

    return (
        <form onSubmit={handleSubmit}>
            <TextInput
                label="Term"
                value={text}
                onChange={(event) => setText(event.currentTarget.value)}
                required
            />

            <Button type="submit" variant="filled" mt={'30px'}>
                Ask
            </Button>
        </form>
    )
}

export default KBExplorer