import {Box, Button, Divider, List, TextInput, Title} from "@mantine/core";
import {useEffect, useState} from "react";
import {useMutation} from "react-query";
import {api} from "../dal/api.js";

const KBExplorer = () => {
    const [text, setText] = useState('');

    const mutation = useMutation({
        mutationFn: ({term}) => {
            return api.getTermInfo({term})
        }
    })

    const handleSubmit = (e) => {
        e.preventDefault()
        mutation.mutate({term: text})
    };

    useEffect(() => {
        console.log(mutation.data)
    }, [mutation.data])


    return (
        <form onSubmit={handleSubmit}>
            <TextInput
                label="Term"
                value={text}
                onChange={(event) => setText(event.currentTarget.value)}
                required
            />

            <Button type="submit" variant="filled" mt={'30px'} loading={mutation.isLoading}>
                Ask
            </Button>

            <AxiomList term={text} type={"argument"} axioms={mutation?.data?.asArgument}/>
            <AxiomList term={text} type={"antecedent"} axioms={mutation?.data?.antecedent}/>
            <AxiomList term={text} type={"consequent"} axioms={mutation?.data?.consequent}/>
            <AxiomList term={text} type={"statement"} axioms={mutation?.data?.statement}/>
        </form>
    )
}


const AxiomList = ({term, axioms, type}) => {
    return (
        <Box>
            {axioms && axioms.length > 0 &&
                <Box>
                    <Divider my={'20px'}/>
                    <Title order={4}>
                        {term} as {type}:
                    </Title>
                </Box>
            }
            <List>
                {axioms && axioms.map(axiom =>
                    <List.Item key={axiom}>{axiom}</List.Item>
                )}
            </List>
        </Box>
    )
}

export default KBExplorer