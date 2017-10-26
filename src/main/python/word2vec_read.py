from scipy import spatial
import json

file_name = "glove.6B.300d.txt"

def distance_between_sentence_and_context(sentence_word_count, context_word_count):
    combined_word_counts = zip(sentence_word_count, context_word_count)
    sentence_sum = sum([count for word, count in sentence_word_count])
    context_sum = sum([count for word, count in context_word_count])
    vectors_count = [((data[sentence_word], sentence_count), (data[context_word], context_count)) for (sentence_word, sentence_count), (context_word, context_count) in combined_word_count]
    vectors_score = [spatial.distance.cosine(sentence_vec, context_vec)*(context_count/context_counts_sum)*(sentence_count/sentence_sum) for (sentence_vec, sentence_count), (context_vec, context_count) in vectors_count]
    final_score = sum(vectores_score)
    return final_score

data = {}

with open(file_name) as f:
    for line in f:
        splitted = line.split()
        token = splitted.pop(0)
        data[token] = ([float(x) for x in splitted])

import json

with open('data.json') as data_file:    
    data = json.load(data_file)
