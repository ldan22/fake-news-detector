import {Tabs, Container} from '@mantine/core';
import FactChecker from "./components/FactChecker.jsx";
import KBExplorer from "./components/KBExplorer.jsx";


function App() {


    return (
        <>

            <Container size="800px" px="xs" mt={'100px'}>
                <Tabs defaultValue="gallery">
                    <Tabs.List>
                        <Tabs.Tab value="gallery">Fact checking</Tabs.Tab>
                        <Tabs.Tab value="messages">Explorer</Tabs.Tab>
                    </Tabs.List>

                    <Tabs.Panel value="gallery" pt="xs">
                        <FactChecker/>
                    </Tabs.Panel>

                    <Tabs.Panel value="messages" pt="xs">
                        <KBExplorer/>
                    </Tabs.Panel>
                </Tabs>
            </Container>
        </>
    );
}

export default App
