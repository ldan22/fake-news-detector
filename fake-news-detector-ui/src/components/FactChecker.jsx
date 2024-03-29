import {Box, Button, Divider, Flex, Grid, Group, List, Radio, TextInput, Title} from "@mantine/core";
import {useState} from "react";
import {useMutation} from "react-query";
import {api} from "../dal/api.js";
import {testData} from "../test/testData.js";

const FactChecker = () => {
    const [text, setText] = useState('');
    const [nlpProcessor, setNlpProcessor] = useState('gpt');

    const factCheckerMutation = useMutation({
        mutationFn: ({text, nlpProcessor}) => {
            return api.checkTruth({text, nlpProcessor})
        }
    })
    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log({text, nlpProcessor})
        factCheckerMutation.mutate({text, nlpProcessor})
    };


    return (
        <>
            <form onSubmit={handleSubmit}>
                <TextInput
                    label="Conjecture"
                    value={text}
                    onChange={(event) => setText(event.currentTarget.value)}
                    required
                />
                <Radio.Group
                    name="nlpProcessor"
                    label="Select the nlp processor"
                    withAsterisk
                    value={nlpProcessor}
                    onChange={setNlpProcessor}
                    mt={'30px'}
                >
                    <Group mt="xs">
                        <Radio value="gpt" label="GPT"/>
                        <Radio value="sigmanlp" label="SigmaNLP"/>
                    </Group>
                </Radio.Group>

                <Button type="submit" variant="filled" mt={'30px'} loading={factCheckerMutation.isLoading}>
                    Check
                </Button>
            </form>
            <Box style={{position: "absolute", top: "150px", right: "50px"}}>
                <Title order={4}>
                    Test data:
                </Title>
                <TestTextsBox setText={setText}/>
            </Box>

            {factCheckerMutation.isError ? (
                <Box mt={'20px'}>An error occurred: {factCheckerMutation.error.message}</Box>
            ) : null}
            {factCheckerMutation.isSuccess ? <CheckerResult data={factCheckerMutation.data?.data}/> : null}
        </>
    )
}

const CheckerResult = ({data}) => {
    console.log(data?.proof)
    return (
        <>
            <Flex my={'20px'}>
                <Title order={5}>
                    Verdict:
                </Title>
                <Box ml={'5px'}>
                    {data?.state}
                </Box>
            </Flex>
            <Flex my={'20px'}>
                <Title order={5}>
                    Elapsed (s):
                </Title>
                <Box ml={'5px'}>
                    {data?.elapsedSeconds}
                </Box>
            </Flex>
            <Box>
                <Title order={5}>
                    Axioms used for proof:
                </Title>
                <List>
                    {data?.proof?.map(step => (
                        <List.Item key={step}>
                            {step}
                        </List.Item>
                    ))}
                </List>
            </Box>
        </>
    )
}


const TestTextsBox = ({setText}) => {
    return (
        <>
            {testData.map(t => (
                <Box key={t.text} onClick={() => setText(t.text)} style={{cursor: "pointer"}}>
                    {t.text}
                    <Divider/>
                </Box>
            ))}
        </>
    )
}


export default FactChecker;