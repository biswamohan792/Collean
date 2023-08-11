 const {google} = require('googleapis');

// let MESSAGE = process.argv[2];
// let DISCOVERY_URL = process.argv[3];
// let API_KEY = process.argv[4];
//
// google.discoverAPI(DISCOVERY_URL)
//     .then(client => {
//       const analyzeRequest = {
//         comment: {
//           text: MESSAGE,
//         },
//         requestedAttributes: {
//           TOXICITY: {},
//           SEVERE_TOXICITY: {},
//           IDENTITY_ATTACK: {},
//           INSULT: {},
//           PROFANITY: {},
//           THREAT: {},
//           SEXUALLY_EXPLICIT: {}
//         },
//       };
//
//       client.comments.analyze(
//           {
//             key: API_KEY,
//             resource: analyzeRequest,
//           },
//           (err, response) => {
//             if (err) throw err;
//             console.log(JSON.stringify({
//               "success":true,
//               "THREAT":response.data["attributeScores"]["THREAT"]["summaryScore"]["value"],
//               "TOXICITY":response.data["attributeScores"]["TOXICITY"]["summaryScore"]["value"],
//               "IDENTITY_ATTACK":response.data["attributeScores"]["IDENTITY_ATTACK"]["summaryScore"]["value"],
//               "SEXUALLY_EXPLICIT":response.data["attributeScores"]["SEXUALLY_EXPLICIT"]["summaryScore"]["value"],
//               "PROFANITY":response.data["attributeScores"]["PROFANITY"]["summaryScore"]["value"],
//               "INSULT":response.data["attributeScores"]["INSULT"]["summaryScore"]["value"],
//               "SEVERE_TOXICITY":response.data["attributeScores"]["SEVERE_TOXICITY"]["summaryScore"]["value"],
//             }));
//           }
//       );
//   })
//   .catch(err => {
//     console.log(JSON.stringify({"success":false}));
//   });

console.log(`{"success":true,"THREAT":0.009048914,"TOXICITY":0.2855005,"IDENTITY_ATTACK":0.012943448,"SEXUALLY_EXPLICIT":0.3738246,"PROFANITY":0.22612886,"INSULT":0.04442204,"SEVERE_TOXICITY":0.0118255615}`);