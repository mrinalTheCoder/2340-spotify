require('dotenv').config();
const express = require('express');
const axios = require('axios');
const app = express();

app.use(express.json());

const openAiUrl = 'https://api.openai.com/v1/engines/davinci/completions';
const openaiApiKey = process.env.OPENAI_API_KEY;
const port = process.env.PORT || 3000;

// Generates LLM content based on a single music taste description
app.post('/generate-text', async (req, res) => {
  const prompt = `Describe someone who listens to ${req.body.prompt}.`;
  try {
    const response = await axios.post(openAiUrl, {
      prompt: prompt,
      max_tokens: 150
    }, {
      headers: { 'Authorization': `Bearer ${openaiApiKey}` }
    });
    res.json({ description: response.data.choices[0].text });
  } catch (error) {
    console.error("Error calling OpenAI API:", error.response ? error.response.data : error);
    res.status(500).send("Failed to generate description");
  }
});

// Compares music tastes by generating descriptions for each and concatenating the results
app.post('/compare-tastes', async (req, res) => {
  const userPrompt = `Describe someone who listens to ${req.body.userPrompt}.`;
  const friendPrompt = `Describe someone who listens to ${req.body.friendPrompt}.`;

  try {
    const userResponse = await axios.post(openAiUrl, {
      prompt: userPrompt,
      max_tokens: 150
    }, { headers: { 'Authorization': `Bearer ${openaiApiKey}` } });

    const friendResponse = await axios.post(openAiUrl, {
      prompt: friendPrompt,
      max_tokens: 150
    }, { headers: { 'Authorization': `Bearer ${openaiApiKey}` } });

    const comparisonResult = `User's taste: ${userResponse.data.choices[0].text}\nFriend's taste: ${friendResponse.data.choices[0].text}`;
    res.json({ comparison: comparisonResult });
  } catch (error) {
    console.error('Error calling OpenAI API:', error.response ? error.response.data : error);
    res.status(500).send('Error generating comparison');
  }
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
