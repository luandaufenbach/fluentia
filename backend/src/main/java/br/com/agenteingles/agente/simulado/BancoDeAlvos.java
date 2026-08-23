package br.com.agenteingles.agente.simulado;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * As frases-alvo do modo simulado, por modulo.
 *
 * <p>Antes so o verbo "to be" tinha banco proprio. Nos outros quinze modulos o desafio
 * saia generico e <b>sem gabarito</b>, e sem gabarito o avaliador simulado devolvia nota
 * fixa: a trilha inteira ficava amarela independentemente do que o aluno escrevesse, e o
 * orquestrador — que decide pelo erro detectado — nao recebia erro nenhum para ler.
 *
 * <p>Nao substitui o gerador com IA. O que ele da e um loop que funciona de ponta a ponta
 * com nota que significa alguma coisa, sem consumir a API.
 *
 * @param tipoDeErroTipico tipo do catalogo que uma resposta errada neste modulo costuma
 *                         produzir — e o que faz o modo simulado exercitar tambem o aviso
 *                         de erro repetido
 */
public record BancoDeAlvos(String tipoDeErroTipico, List<Alvo> alvos) {

    /**
     * Uma frase a ser produzida.
     *
     * @param emPortugues o que o enunciado pede
     * @param emIngles a resposta de referencia, natural e correta
     */
    public record Alvo(String emPortugues, String emIngles) {
    }

    private static final Map<String, BancoDeAlvos> POR_MODULO = montar();

    /** @return o banco do modulo, ou {@code null} quando ele ainda nao tem um */
    public static BancoDeAlvos doModulo(String codigoDoModulo) {
        return POR_MODULO.get(codigoDoModulo);
    }

    private static BancoDeAlvos banco(String tipoDeErro, String... paresPortuguesIngles) {
        List<Alvo> alvos = new java.util.ArrayList<>();
        for (int i = 0; i < paresPortuguesIngles.length; i += 2) {
            alvos.add(new Alvo(paresPortuguesIngles[i], paresPortuguesIngles[i + 1]));
        }
        return new BancoDeAlvos(tipoDeErro, List.copyOf(alvos));
    }

    private static Map<String, BancoDeAlvos> montar() {
        Map<String, BancoDeAlvos> bancos = new LinkedHashMap<>();

        bancos.put("verbo_to_be", banco("concordancia_do_verbo_to_be",
                "Eu sou brasileiro.", "I am Brazilian.",
                "Ela e a minha gerente.", "She is my manager.",
                "Nos estamos atrasados.", "We are late.",
                "Eles nao estao prontos.", "They are not ready.",
                "Voce e o novo desenvolvedor?", "Are you the new developer?",
                "O quarto nao esta limpo.", "The room is not clean.",
                "Eu nao estou com fome.", "I am not hungry.",
                "Ele esta no aeroporto.", "He is at the airport.",
                "O teste esta quebrado.", "The test is broken.",
                "Esta e a minha primeira vez aqui.", "This is my first time here."));

        bancos.put("artigos", banco("uso_de_artigo",
                "Eu quero uma maca.", "I want an apple.",
                "Ela e uma engenheira.", "She is an engineer.",
                "O hotel fica perto da estacao.", "The hotel is near the station.",
                "Eu gosto de musica.", "I like music.",
                "Ele e um estudante universitario.", "He is a university student.",
                "Passe o sal, por favor.", "Pass the salt, please.",
                "Eu preciso de um guarda-chuva.", "I need an umbrella.",
                "A reuniao comeca em uma hora.", "The meeting starts in an hour."));

        bancos.put("pronomes_pessoais", banco("pronome_errado",
                "Ela me deu o livro.", "She gave me the book.",
                "Nos os vimos ontem.", "We saw them yesterday.",
                "Eu falei com ele.", "I talked to him.",
                "Eles nos ajudaram.", "They helped us.",
                "Voce pode me ligar depois?", "Can you call me later?",
                "Eu a conheco ha anos.", "I have known her for years.",
                "Este presente e para voce.", "This gift is for you.",
                "Ele nao gosta de nos.", "He does not like us."));

        bancos.put("presente_simples", banco("terceira_pessoa_do_singular",
                "Ela trabalha em um banco.", "She works at a bank.",
                "Eu acordo as sete horas.", "I wake up at seven.",
                "Ele nao come carne.", "He does not eat meat.",
                "Voce mora aqui?", "Do you live here?",
                "O time joga aos domingos.", "The team plays on Sundays.",
                "Nos estudamos ingles toda noite.", "We study English every night.",
                "Ela sempre chega cedo.", "She always arrives early.",
                "O escritorio abre as nove.", "The office opens at nine."));

        bancos.put("passado_simples", banco("verbo_irregular",
                "Eu comprei um livro ontem.", "I bought a book yesterday.",
                "Ela foi ao medico na semana passada.", "She went to the doctor last week.",
                "Nos nao vimos o filme.", "We did not see the movie.",
                "Voce falou com o gerente?", "Did you talk to the manager?",
                "Ele dormiu cedo ontem a noite.", "He slept early last night.",
                "Eles chegaram atrasados.", "They arrived late.",
                "Eu escrevi o relatorio na sexta.", "I wrote the report on Friday.",
                "O time perdeu a partida.", "The team lost the match."));

        bancos.put("comparativos", banco("comparativo_ou_superlativo",
                "Este hotel e mais barato que o outro.", "This hotel is cheaper than the other one.",
                "Ela e a melhor da turma.", "She is the best in the class.",
                "Hoje esta mais quente que ontem.", "Today is hotter than yesterday.",
                "Este e o filme mais interessante do ano.", "This is the most interesting movie of the year.",
                "Meu notebook e mais rapido que o seu.", "My laptop is faster than yours.",
                "Essa foi a pior reuniao da semana.", "That was the worst meeting of the week.",
                "O trem e mais confortavel que o onibus.", "The train is more comfortable than the bus.",
                "Ele fala ingles melhor do que eu.", "He speaks English better than I do."));

        bancos.put("there_is_are", banco("there_is_there_are",
                "Ha um restaurante na esquina.", "There is a restaurant on the corner.",
                "Existem tres quartos disponiveis.", "There are three rooms available.",
                "Nao ha leite na geladeira.", "There is no milk in the fridge.",
                "Ha alguem na sala?", "Is there anyone in the room?",
                "Existem muitos erros neste codigo.", "There are many bugs in this code.",
                "Nao havia ninguem na reuniao.", "There was nobody at the meeting.",
                "Ha um problema com o pagamento.", "There is a problem with the payment.",
                "Existem duas opcoes.", "There are two options."));

        bancos.put("presente_perfeito", banco("presente_perfeito",
                "Eu moro aqui ha cinco anos.", "I have lived here for five years.",
                "Ela ja terminou o relatorio.", "She has already finished the report.",
                "Nos nunca estivemos em Londres.", "We have never been to London.",
                "Voce ja almocou?", "Have you had lunch yet?",
                "Ele trabalha nesta empresa desde 2020.", "He has worked at this company since 2020.",
                "Eu ainda nao li o e-mail.", "I have not read the email yet.",
                "Eles acabaram de chegar.", "They have just arrived.",
                "Voce ja viu esse filme?", "Have you seen this movie?"));

        bancos.put("condicionais_basicos", banco("condicional",
                "Se chover, eu fico em casa.", "If it rains, I will stay home.",
                "Se eu tivesse tempo, eu viajaria.", "If I had time, I would travel.",
                "Se voce estudar, voce vai passar.", "If you study, you will pass.",
                "Se eu fosse voce, eu aceitaria.", "If I were you, I would accept.",
                "Nos sairemos se o trabalho terminar.", "We will leave if the work is done.",
                "Se ela soubesse, ela ajudaria.", "If she knew, she would help.",
                "Se o teste falhar, o deploy para.", "If the test fails, the deploy stops.",
                "Eu compraria a casa se tivesse dinheiro.", "I would buy the house if I had the money."));

        bancos.put("phrasal_verbs_comuns", banco("phrasal_verb",
                "Eu acordo as seis todo dia.", "I get up at six every day.",
                "Desligue a luz, por favor.", "Turn off the light, please.",
                "Nos precisamos remarcar a reuniao.", "We need to put off the meeting.",
                "Ela desistiu do curso.", "She gave up the course.",
                "Voce pode me buscar no aeroporto?", "Can you pick me up at the airport?",
                "Estou procurando as minhas chaves.", "I am looking for my keys.",
                "O aviao decolou atrasado.", "The plane took off late.",
                "Vamos continuar amanha.", "Let's carry on tomorrow."));

        bancos.put("passado_perfeito", banco("passado_perfeito",
                "Quando cheguei, ela ja tinha saido.", "When I arrived, she had already left.",
                "Eu nunca tinha visto aquilo antes.", "I had never seen that before.",
                "Eles ja tinham terminado o projeto.", "They had already finished the project.",
                "Ele disse que tinha perdido o voo.", "He said he had missed the flight.",
                "Nos ja tinhamos comido quando ela ligou.", "We had already eaten when she called.",
                "O sistema tinha caido antes do deploy.", "The system had gone down before the deploy.",
                "Ela percebeu que tinha esquecido a senha.", "She realized she had forgotten the password.",
                "Eu ja tinha lido o contrato.", "I had already read the contract."));

        bancos.put("voz_passiva", banco("voz_passiva",
                "O relatorio foi enviado ontem.", "The report was sent yesterday.",
                "Esta casa foi construida em 1990.", "This house was built in 1990.",
                "O problema esta sendo resolvido.", "The problem is being solved.",
                "As chaves foram perdidas.", "The keys were lost.",
                "O codigo e revisado por dois desenvolvedores.", "The code is reviewed by two developers.",
                "A reuniao foi cancelada.", "The meeting was cancelled.",
                "O e-mail sera enviado amanha.", "The email will be sent tomorrow.",
                "O bug ja foi corrigido.", "The bug has already been fixed."));

        bancos.put("condicionais_mistos", banco("condicional",
                "Se eu tivesse estudado, eu estaria aprovado agora.",
                "If I had studied, I would be approved now.",
                "Se ela nao tivesse saido, ela estaria aqui.",
                "If she had not left, she would be here.",
                "Se tivessemos comecado antes, ja teriamos terminado.",
                "If we had started earlier, we would have finished already.",
                "Se ele fosse mais organizado, nao teria perdido o prazo.",
                "If he were more organized, he would not have missed the deadline.",
                "Se eu falasse ingles, teria aceitado aquele emprego.",
                "If I spoke English, I would have accepted that job.",
                "Se voce tivesse avisado, eu estaria pronto.",
                "If you had told me, I would be ready."));

        bancos.put("subjuntivo", banco("subjuntivo",
                "Eu sugiro que ele chegue mais cedo.", "I suggest that he arrive earlier.",
                "E importante que voce esteja presente.", "It is important that you be present.",
                "Ela pediu que nos ficassemos.", "She asked that we stay.",
                "Se eu fosse rico, eu viajaria o mundo.", "If I were rich, I would travel the world.",
                "O gerente exigiu que o relatorio fosse revisado.",
                "The manager demanded that the report be reviewed.",
                "Eu preferiria que voce nao fizesse isso.", "I would rather you did not do that."));

        bancos.put("inversao", banco("inversao",
                "Nunca vi algo assim.", "Never have I seen anything like this.",
                "Raramente ela chega atrasada.", "Rarely does she arrive late.",
                "Nao so ele chegou tarde como esqueceu os documentos.",
                "Not only did he arrive late, but he also forgot the documents.",
                "So depois percebi o erro.", "Only later did I realize the mistake.",
                "Em hipotese alguma devemos ignorar isso.",
                "Under no circumstances should we ignore this.",
                "Mal ela saiu, o telefone tocou.", "Hardly had she left when the phone rang."));

        bancos.put("expressoes_idiomaticas", banco("expressao_idiomatica",
                "Custa os olhos da cara.", "It costs an arm and a leg.",
                "Vamos deixar para la.", "Let's let it go.",
                "Ele quebrou o galho.", "He helped me out.",
                "Nao e nada de mais.", "It is not a big deal.",
                "Estou de olho nisso.", "I am keeping an eye on that.",
                "Isso caiu do ceu.", "That came out of the blue.",
                "Vamos direto ao ponto.", "Let's get to the point.",
                "Ele deu para tras.", "He backed out."));

        return Map.copyOf(bancos);
    }
}
