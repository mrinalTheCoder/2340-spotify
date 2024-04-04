require('dotenv').config();
const express = require('express');
const axios = require('axios');
const app = express();

app.use(express.json());

const openAiUrl = 'https://api.openai.com/v1/engines/davinci/completions';
const openaiApiKey = process.env.OPENAI_API_KEY; // Ensure this is set in your .env file
const port = process.env.PORT || 3000;

app.post('/generate-text', async (req, res) => {
  try {
    const openaiResponse = await axios.post(
      openAiUrl,
      {
        prompt: req.body.prompt,
        max_tokens: 100
      },
      {
        headers: {
          'Authorization': `Bearer ${openaiApiKey}`
        }
      }
    );
    res.json(openaiResponse.data);
  } catch (error) {
    console.error("Error calling OpenAI API:", error.response ? error.response.data : error.message);
    res.status(500).send("Failed to generate text");
  }
});


app.post('/compare-tastes', async (req, res) => {
  const { userPrompt, friendPrompt } = req.body;

  try {
    // Request to OpenAI API for the user's prompt
    const userResponse = await axios.post(
      openAiUrl,
      { prompt: userPrompt, max_tokens: 100 },
      { headers: { Authorization: `Bearer ${openaiApiKey}` } }
    );

    // Request to OpenAI API for the friend's prompt
    const friendResponse = await axios.post(
      openAiUrl,
      { prompt: friendPrompt, max_tokens: 100 },
      { headers: { Authorization: `Bearer ${openaiApiKey}` } }
    );

    // Construct a comparison result (this part is up to you; this example just concatenates them)
    const comparisonResult = `User's music taste: ${userResponse.data.choices[0].text}\n` +
                             `Friend's music taste: ${friendResponse.data.choices[0].text}`;

    // Send back the comparison result
    res.json({ comparisonResult });
  } catch (error) {
    console.error('Error calling OpenAI API:', error.response ? error.response.data : error.message);
    res.status(500).send('Error generating comparison');
  }
});

const PORT = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`Server running on port ${PORT}`);
});
